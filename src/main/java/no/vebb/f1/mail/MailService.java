package no.vebb.f1.mail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.mail.mailOption.MailOption;
import no.vebb.f1.mail.mailOption.MailOptionEntity;
import no.vebb.f1.mail.mailOption.MailOptionRepository;
import no.vebb.f1.mail.mailPreference.MailPreferenceEntity;
import no.vebb.f1.mail.mailPreference.MailPreferenceId;
import no.vebb.f1.mail.mailPreference.MailPreferenceRepository;
import no.vebb.f1.mail.mailingList.MailingListEntity;
import no.vebb.f1.mail.mailingList.MailingListRepository;
import no.vebb.f1.mail.notified.NotifiedEntity;
import no.vebb.f1.mail.notified.NotifiedRepository;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.user.*;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.UserNotifiedCount;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.util.exception.InvalidEmailException;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;
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
    private final NotifiedRepository notifiedRepository;
    private final UserRespository userRespository;
    private final AdminRepository adminRepository;
    private final MailOptionRepository mailOptionRepository;
    private final MailPreferenceRepository mailPreferenceRepository;
    private final YearService yearService;
    private final RaceService raceService;
    private final CutoffService cutoffService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(MailingListRepository mailingListRepository, JavaMailSender mailSender, NotifiedRepository notifiedRepository, UserRespository userRespository, AdminRepository adminRepository, MailOptionRepository mailOptionRepository, MailPreferenceRepository mailPreferenceRepository, YearService yearService, RaceService raceService, CutoffService cutoffService) {
        this.mailingListRepository = mailingListRepository;
        this.mailSender = mailSender;
        this.notifiedRepository = notifiedRepository;
        this.userRespository = userRespository;
        this.adminRepository = adminRepository;
        this.mailOptionRepository = mailOptionRepository;
        this.mailPreferenceRepository = mailPreferenceRepository;
        this.yearService = yearService;
        this.raceService = raceService;
        this.cutoffService = cutoffService;
    }

    public void addToMailingList(UUID userId, Email email) {
        mailingListRepository.save(new MailingListEntity(userId, email));
    }

    @Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
    public void notifyUsers() {
        try {
            RaceOrderEntity race = raceService.getLatestRaceForPlaceGuess(yearService.getCurrentYear());
            RaceId raceId = race.raceId();
            long timeLeft = cutoffService.getTimeLeftToGuessRace(raceId);
            if (timeLeft < 0) {
                return;
            }
            int timeLeftHours = (int) (timeLeft / 3600);
            List<UserMail> mailingList = getMailingList(raceId);
            List<NotifiedEntity> notifications = new ArrayList<>();
            for (UserMail user : mailingList) {
                UUID userId = user.userEntity().id();
                int notifiedCount = notifiedRepository.countAllByRaceIdAndUserId(raceId, userId);
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
                        message.setFrom(new InternetAddress(fromEmail, "F1 Gjettespill"));
                        message.addRecipients(Message.RecipientType.TO, user.email().toString());
                        message.setSubject("F1 Gjettespill påminnelse", "UTF-8");
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
        } catch (InvalidYearException | EmptyResultDataAccessException | NoAvailableRaceException ignored) {
        }
    }

    private String getMessageContent(UserMail user, RaceOrderEntity race, int timeLeft) {
        String greet = String.format("Hei %s!", user.userEntity().username());
        String reminder = String.format("Dette er en påminnelse om å gjette på %s før tiden går ut.", race.name());
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

    public Email getEmail(UUID userId) {
        return mailingListRepository.findById(userId).map(MailingListEntity::email).orElse(null);
    }


    public void sendVerificationCodeMail(UserMail user, int verificationCode) {
        String strCode = String.valueOf(verificationCode);
        String formattedCode = String.format("%s %s %s",
                strCode.substring(0, 3), strCode.substring(3, 6), strCode.substring(6, 9));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(fromEmail, "F1 Gjettespill"));
            message.addRecipients(Message.RecipientType.TO, user.email().toString());
            message.setSubject("Verifikasjonskode F1 Gjettespill");
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
                message.setFrom(new InternetAddress(fromEmail, "F1 Gjettespill"));
                message.addRecipients(Message.RecipientType.TO, admin.email().toString());
                message.setSubject("Server melding F1 Gjettespill");
                message.setContent(String.format("Hei administrator!\n\nDette er en automatisk generert melding:\n%s",
                        messageForAdmin), "text/plain; charset=UTF-8");
                mailSender.send(message);
                logger.info("Successfully sent server message to '{}'", admin.userEntity().id());
            } catch (MessagingException | UnsupportedEncodingException ignored) {
            }
        }
    }

    public void addMailOption(UUID userId, MailOption option) {
        mailPreferenceRepository.save(new MailPreferenceEntity(new MailPreferenceId(userId, option)));
    }

    public void removeMailOption(UUID userId, MailOption option) {
        mailPreferenceRepository.deleteById(new MailPreferenceId(userId, option));
    }

    public void clearMailPreferences(UUID userId) {
        mailPreferenceRepository.deleteByIdUserId(userId);
    }

    public List<MailOption> getMailingPreference(UUID userId) {
        return mailPreferenceRepository.findAllByIdUserIdOrderByIdMailOption(userId).stream()
                .map(MailPreferenceEntity::mailOption)
                .toList();
    }

    public List<MailOption> getMailingOptions() {
        return mailOptionRepository.findAllByOrderByMailOption().stream()
                .map(MailOptionEntity::mailOption)
                .toList();
    }

    public List<UserNotifiedCount> userDataNotified(UUID userId) {
        return notifiedRepository.findAllByUserId(userId).stream()
                .map(UserNotifiedCount::fromIUserNotifiedCount)
                .toList();
    }

    public List<UserMail> getMailingList(RaceId raceId) {
        return mailingListRepository.findAllByRaceId(raceId).stream()
                .map(user ->
                        new UserMail(
                                user.user(),
                                user.email()
                        )
                )
                .toList();
    }

    public MailOption getMailOption(int option) {
        return mailOptionRepository.findById(new MailOption(option)).orElseThrow(InvalidEmailException::new).mailOption();
    }
}
