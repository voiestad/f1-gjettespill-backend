package no.vebb.f1.components;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserMail;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
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
			if (timeLeft > 3600 || timeLeft < 0) {
				return;
			}
			List<UserMail> mailingList = db.getMailingList(raceId);
			for (UserMail user : mailingList) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(fromEmail);
				message.setTo(user.email);
				message.setSubject("F1 Tipping påminnelse");
				message.setText(
					String.format("Hei %s!\n\nDette er en påminnelse om å tippe på %s før tiden går ut.", user.user.username, race.name));
				mailSender.send(message);
				UUID userId = user.user.id;
				db.setNotified(raceId, userId);
				logger.info("Successfully notified '{}' about '{}'", userId, race.name);
			}
		} catch (InvalidYearException e) {
		} catch (EmptyResultDataAccessException e) {
		}
	}
}
