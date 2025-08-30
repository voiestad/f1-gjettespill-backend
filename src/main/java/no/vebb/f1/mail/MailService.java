package no.vebb.f1.mail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import no.vebb.f1.database.Database;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.user.*;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.MailOption;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.year.YearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MailService {

    private final MailingListRepository mailingListRepository;
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;
    private final Database db;
    private final NotifiedRepository notifiedRepository;
    private final UserRespository userRespository;
    private final AdminRepository adminRepository;
    private final MailOptionRepository mailOptionRepository;
    private final MailPreferenceRepository mailPreferenceRepository;
    private final YearService yearService;
    private final RaceService raceService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(MailingListRepository mailingListRepository, JavaMailSender mailSender, Database db, NotifiedRepository notifiedRepository, UserRespository userRespository, AdminRepository adminRepository, MailOptionRepository mailOptionRepository, MailPreferenceRepository mailPreferenceRepository, YearService yearService, RaceService raceService) {
        this.mailingListRepository = mailingListRepository;
        this.mailSender = mailSender;
        this.db = db;
        this.notifiedRepository = notifiedRepository;
        this.userRespository = userRespository;
        this.adminRepository = adminRepository;
        this.mailOptionRepository = mailOptionRepository;
        this.mailPreferenceRepository = mailPreferenceRepository;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    public void addToMailingList(UUID userId, String email) {
        mailingListRepository.save(new MailEntity(userId, email));
    }

    @Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
    public void notifyUsers() {
        try {
            RaceOrderEntity race = raceService.getLatestRaceForPlaceGuess(new Year(TimeUtil.getCurrentYear(), yearService));
            RaceId raceId = new RaceId(race.raceId());
            long timeLeft = db.getTimeLeftToGuessRace(raceId);
            if (timeLeft < 0) {
                return;
            }
            int timeLeftHours = (int) (timeLeft / 3600);
            List<UserMail> mailingList = db.getMailingList(raceId);
            List<NotifiedEntity> notifications = new ArrayList<>();
            for (UserMail user : mailingList) {
                UUID userId = user.userEntity().id();
                int notifiedCount = notifiedRepository.countAllByRaceIdAndUserId(raceId.value, userId);
                List<MailOption> options = getMailingPreference(userId);
                for (MailOption option : options) {
                    if (notifiedCount > 0) {
                        notifiedCount--;
                        continue;
                    }
                    if (option.value <= timeLeftHours) {
                        break;
                    }
                    try {
                        MimeMessage message = mailSender.createMimeMessage();
                        message.setFrom(new InternetAddress(fromEmail, "F1 Tipping"));
                        message.addRecipients(Message.RecipientType.TO, user.email());
                        message.setSubject("F1 Tipping påminnelse", "UTF-8");
                        message.setContent(getMessageContent(user, race, option.value), "text/plain; charset=UTF-8");
                        mailSender.send(message);
                        notifications.add(new NotifiedEntity(userId, raceId));
                        logger.info("Successfully notified '{}' about '{}'", userId, race.name());
                    } catch (MessagingException e) {
                        logger.info("Message fail");
                    } catch (UnsupportedEncodingException e) {
                        logger.info("Encoding fail");
                    }
                    break;
                }
            }
            notifiedRepository.saveAll(notifications);
        } catch (InvalidYearException | EmptyResultDataAccessException ignored) {
        }
    }

    private String getMessageContent(UserMail user, RaceOrderEntity race, int timeLeft) {
        String greet = String.format("Hei %s!", user.userEntity().username());
        String reminder = String.format("Dette er en påminnelse om å tippe på %s før tiden går ut.", race.name());
        String hours = timeLeft == 1 ? "time" : "timer";
        String time = String.format("Det er mindre enn %d %s igjen.", timeLeft, hours);
        return String.format("%s\n\n%s %s", greet, reminder, time);
    }

    public void clearUserFromMailing(UUID userId) {
        clearMailPreferences(userId);
        clearNotified(userId);
        mailingListRepository.deleteById(userId);
    }

    private void clearNotified(UUID userId) {
        notifiedRepository.deleteByUserId(userId);
    }

    public boolean userHasEmail(UUID userId) {
        return mailingListRepository.existsById(userId);
    }

    public String getEmail(UUID userId) {
        return mailingListRepository.findById(userId).map(MailEntity::email).orElse(null);
    }


    public void sendVerificationCodeMail(UserMail user, int verificationCode) {
        String strCode = String.valueOf(verificationCode);
        String formattedCode = String.format("%s %s %s",
                strCode.substring(0, 3), strCode.substring(3, 6), strCode.substring(6, 9));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(fromEmail, "F1 Tipping"));
            message.addRecipients(Message.RecipientType.TO, user.email());
            message.setSubject("Verifikasjonskode F1 Tipping");
            message.setContent(String.format("Hei %s!\n\nHer er din verifikasjonskode: %s\n\nDen er gyldig i 10 minutter.",
                    user.userEntity().username(), formattedCode), "text/plain; charset=UTF-8");
            mailSender.send(message);
            logger.info("Successfully sent verification code to '{}'", user.userEntity().id());
        } catch (MessagingException | UnsupportedEncodingException ignored) {
        }
    }

    public void sendServerMessageToAdmins(String messageForAdmin) {
        List<UUID> admins = adminRepository.findAll().stream().map(AdminEntity::id).toList();
        List<UserMail> adminsWithMail = userRespository.findAllById(admins).stream()
                .filter(user -> userHasEmail(user.id()))
                .map(admin -> new UserMail(admin, getEmail(admin.id())))
                .toList();
        for (UserMail admin : adminsWithMail) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                message.setFrom(new InternetAddress(fromEmail, "F1 Tipping"));
                message.addRecipients(Message.RecipientType.TO, admin.email());
                message.setSubject("Server melding F1 Tipping");
                message.setContent(String.format("Hei administrator!\n\nDette er en automatisk generert melding:\n%s",
                        messageForAdmin), "text/plain; charset=UTF-8");
                mailSender.send(message);
                logger.info("Successfully sent server message to '{}'", admin.userEntity().id());
            } catch (MessagingException | UnsupportedEncodingException ignored) {
            }
        }
    }

    public boolean isValidMailOption(int option) {
        return mailOptionRepository.existsById(option);
    }

    public void addMailOption(UUID userId, MailOption option) {
        mailPreferenceRepository.save(new MailPreferenceEntity(new MailPreferenceId(userId, option.value)));
    }

    public void removeMailOption(UUID userId, MailOption option) {
        mailPreferenceRepository.deleteById(new MailPreferenceId(userId, option.value));
    }

    public void clearMailPreferences(UUID userId) {
        mailPreferenceRepository.deleteByIdUserId(userId);
    }

    public List<MailOption> getMailingPreference(UUID userId) {
        return mailPreferenceRepository.findAllByIdUserIdOrderByIdMailOption(userId).stream()
                .map(MailPreferenceEntity::mailOption)
                .map(MailOption::new)
                .toList();
    }

    public List<MailOption> getMailingOptions() {
        return mailOptionRepository.findAllByOrderByMailOption().stream()
                .map(MailOptionEntity::mailOption)
                .map(MailOption::new)
                .toList();
    }
}
