package no.vebb.f1.components;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserMail;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.domainPrimitive.MailOption;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Component
public class NotificationMail {

	private static final Logger logger = LoggerFactory.getLogger(NotificationMail.class);

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Database db;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Scheduled(fixedRate = 300000, initialDelay = 1000)
	public void notifyUsers() {
		try {
			CutoffRace race = db.getLatestRaceForPlaceGuess(new Year(TimeUtil.getCurrentYear(), db));
			RaceId raceId = race.id;
			long timeLeft = db.getTimeLeftToGuessRace(raceId);
			if (timeLeft < 0) {
				return;
			}
			int timeLeftHours = (int) (timeLeft / 3600);
			List<UserMail> mailingList = db.getMailingList(raceId);
			for (UserMail user : mailingList) {
				UUID userId = user.user.id;
				int notifiedCount = db.getNotifiedCount(raceId, userId);
				List<MailOption> options = db.getMailingPreference(userId);
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
						message.addRecipients(RecipientType.TO, user.email);
						message.setSubject("F1 Tipping påminnelse", "UTF-8");
						message.setContent(getMessageContent(user, race, option.value), "text/plain; charset=UTF-8");
						mailSender.send(message);
						db.setNotified(raceId, userId);
						logger.info("Successfully notified '{}' about '{}'", userId, race.name);
					} catch (MessagingException e) {
						e.printStackTrace();
						logger.info("Message fail");
					} catch (UnsupportedEncodingException e) {
						logger.info("Encoding fail");
					}
					break;
				}
			}
		} catch (InvalidYearException e) {
		} catch (EmptyResultDataAccessException e) {
		}
	}

	private String getMessageContent(UserMail user, CutoffRace race, int timeLeft) {
		String greet = String.format("Hei %s!", user.user.username);
		String reminder = String.format("Dette er en påminnelse om å tippe på %s før tiden går ut.", race.name);
		String hours = timeLeft == 1 ? "time" : "timer";
		String time = String.format("Det er mindre enn %d %s igjen.", timeLeft, hours);
		return String.format("%s\n\n%s %s", greet, reminder, time);
	}
}
