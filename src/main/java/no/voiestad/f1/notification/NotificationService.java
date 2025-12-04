package no.voiestad.f1.notification;

import java.util.*;

import no.voiestad.f1.collection.Race;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.notification.guessReminderOption.*;
import no.voiestad.f1.notification.guessReminderPreference.*;
import no.voiestad.f1.notification.notified.*;
import no.voiestad.f1.notification.ntfy.*;
import no.voiestad.f1.notification.ntfy.message.*;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.user.admin.AdminEntity;
import no.voiestad.f1.user.admin.AdminRepository;
import no.voiestad.f1.util.TimeUtil;
import no.voiestad.f1.collection.UserNotifiedCount;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotifiedRepository notifiedRepository;
    private final GuessReminderOptionRepository guessReminderOptionRepository;
    private final GuessReminderPreferenceRepository guessReminderPreferenceRepository;
    private final YearService yearService;
    private final RaceService raceService;
    private final CutoffService cutoffService;
    private final NtfySender ntfySender;
    private final NtfyTopicRepository ntfyTopicRepository;
    private final AdminRepository adminRepository;

    public NotificationService(
            NotifiedRepository notifiedRepository,
            GuessReminderOptionRepository guessReminderOptionRepository,
            GuessReminderPreferenceRepository guessReminderPreferenceRepository,
            YearService yearService,
            RaceService raceService,
            CutoffService cutoffService,
            NtfySender ntfySender,
            NtfyTopicRepository ntfyTopicRepository,
            AdminRepository adminRepository) {
        this.notifiedRepository = notifiedRepository;
        this.guessReminderOptionRepository = guessReminderOptionRepository;
        this.guessReminderPreferenceRepository = guessReminderPreferenceRepository;
        this.yearService = yearService;
        this.raceService = raceService;
        this.cutoffService = cutoffService;
        this.ntfySender = ntfySender;
        this.ntfyTopicRepository = ntfyTopicRepository;
        this.adminRepository = adminRepository;
    }

    public UUID addNtfyTopic(UUID userId) {
        UUID topic = UUID.randomUUID();
        ntfyTopicRepository.save(new NtfyTopicEntity(userId, topic));
        return topic;
    }

    @Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
    public void guessReminders() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return;
        }
        Year year = optYear.get();
        Optional<Race> optRace = raceService.getUpcomingRace(year);
        if (optRace.isEmpty()) {
            return;
        }
        Race race = optRace.get();
        RaceId raceId = race.id();
        long timeLeft = cutoffService.getTimeLeftToGuessRace(raceId);
        if (timeLeft < 0) {
            return;
        }
        long timeLeftPreRace = cutoffService.getTimeLeftToGuessPreRace(raceId);
        boolean isPreRace = timeLeftPreRace > 0;
        Optional<Race> optNewestStartingGridRace = raceService.getLatestRaceForPlaceGuess(year);
        if (!isPreRace) {
            if (optNewestStartingGridRace.isEmpty()) {
                return;
            }
            if (!optNewestStartingGridRace.get().id().equals(raceId)) {
                return;
            }
        }
        int timeLeftHours = isPreRace ? (int) (timeLeftPreRace / 3600) : (int) (timeLeft / 3600);
        List<NtfyTopicEntity> usersLeftToGuess = getUsersLeftToGuess(raceId, isPreRace);
        List<NotifiedEntity> notifications = new ArrayList<>();
        for (NtfyTopicEntity user : usersLeftToGuess) {
            UUID userId = user.userId();
            int notifiedCount = notifiedRepository.countAllByRaceIdAndUserId(raceId, userId);
            List<GuessReminderOption> options = new ArrayList<>(getGuessReminderPreference(userId));
            Collections.reverse(options);
            for (GuessReminderOption option : options) {
                if (notifiedCount > 0) {
                    notifiedCount--;
                    continue;
                }
                if (option.value() <= timeLeftHours) {
                    break;
                }
                String hours = option.value() == 1 ? "time" : "timer";
                String messageBody = String.format("Det er mindre enn %s %s igjen på å gjette på %s i %s",
                        option.value(), hours, isPreRace ? "pole og 1. plass" : "10. plass", race.name());
                Optional<NtfyMessage> message = new NtfyMessageBuilder()
                        .setTopic(user.topic().toString())
                        .setTitle("Husk å gjette!")
                        .setMessage(messageBody)
                        .setTags("racing_car")
                        .build();
                if (message.isPresent() && ntfySender.send(message.get())) {
                    notifications.add(new NotifiedEntity(userId, raceId));
                    logger.info("Successfully notified user");
                } else {
                    logger.warn("Failed to notify user");
                }
                break;
            }
        }
        notifiedRepository.saveAll(notifications);
    }

    public boolean testNotification(UUID userId) {
        Optional<NtfyTopicEntity> optTopic = ntfyTopicRepository.findById(userId);
        if (optTopic.isEmpty()) {
            return false;
        }
        Optional<NtfyMessage> message = new NtfyMessageBuilder()
                .setTopic(optTopic.get().topic().toString())
                .setTitle("Testvarsling!")
                .setMessage("Dette er en test")
                .setTags("racing_car")
                .build();
        return message.filter(ntfySender::send).isPresent();
    }

    public void clearUserFromNtfy(UUID userId) {
        clearGuessReminderPreferences(userId);
        clearNotified(userId);
        ntfyTopicRepository.deleteById(userId);
    }

    public void clearNotified() {
        notifiedRepository.deleteAll();
    }

    private void clearNotified(UUID userId) {
        notifiedRepository.deleteByUserId(userId);
    }

    public Optional<UUID> getNtfyTopic(UUID userId) {
        return ntfyTopicRepository.findById(userId).map(NtfyTopicEntity::topic);
    }

    public void sendServerMessageToAdmins(String messageForAdmin) {
        List<UUID> adminsWithNtfy = adminRepository.findAll().stream()
                .map(AdminEntity::id)
                .map(this::getNtfyTopic)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        for (UUID admin : adminsWithNtfy) {
            Optional<NtfyMessage> message = new NtfyMessageBuilder()
                    .setTopic(admin.toString())
                    .setTitle("Servermelding F1 Gjettespill")
                    .setMessage(messageForAdmin)
                    .build();
            if (message.isPresent()) {
                ntfySender.send(message.get());
                logger.info("Successfully sent server message to '{}'", admin);
            } else {
                logger.warn("Failed to send server message to '{}'", admin);
            }
        }
    }

    public void addGuessReminderOption(UUID userId, GuessReminderOption option) {
        guessReminderPreferenceRepository.save(new GuessReminderPreferenceEntity(new GuessReminderPreferenceId(userId, option)));
    }

    public void removeGuessReminderOption(UUID userId, GuessReminderOption option) {
        guessReminderPreferenceRepository.deleteById(new GuessReminderPreferenceId(userId, option));
    }

    public void clearGuessReminderPreferences(UUID userId) {
        guessReminderPreferenceRepository.deleteByIdUserId(userId);
    }

    public List<GuessReminderOption> getGuessReminderPreference(UUID userId) {
        return guessReminderPreferenceRepository.findAllByIdUserIdOrderByIdOption(userId).stream()
                .map(GuessReminderPreferenceEntity::option)
                .toList();
    }

    public List<GuessReminderOption> getGuessReminderOptions() {
        return guessReminderOptionRepository.findAllByOrderByOption().stream()
                .map(GuessReminderOptionEntity::option)
                .toList();
    }

    public List<UserNotifiedCount> userDataNotified(UUID userId) {
        return notifiedRepository.findAllByUserId(userId).stream()
                .map(UserNotifiedCount::fromIUserNotifiedCount)
                .toList();
    }

    public List<NtfyTopicEntity> getUsersLeftToGuess(RaceId raceId, boolean isPreRace) {
        return isPreRace ? ntfyTopicRepository.findAllByRaceIdPreRace(raceId) :
                ntfyTopicRepository.findAllByRaceIdRace(raceId);
    }

    public Optional<GuessReminderOption> getGuessReminderOption(int option) {
        return guessReminderOptionRepository.findById(new GuessReminderOption(option)).map(GuessReminderOptionEntity::option);
    }
}
