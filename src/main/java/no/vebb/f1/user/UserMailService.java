package no.vebb.f1.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.VerificationCodeGenerator;

@Service
public class UserMailService {

	private static final Logger logger = LoggerFactory.getLogger(UserMailService.class);

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Database db;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendVerificationCode(UserMail user) {
		final int verificationCode = VerificationCodeGenerator.getCode();
		db.addVerificationCode(user, verificationCode);
		String strCode = String.valueOf(verificationCode);
		String formattedCode = String.format("%s %s %s",
				strCode.substring(0, 3), strCode.substring(3, 6), strCode.substring(6, 9));
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(user.email);
		message.setSubject("Verifikasjonskode F1 Tipping");
		message.setText(
				String.format("Hei %s!\n\nHer er din verifikasjonskode: %s\n\nDen er gyldig i 10 minutter.", user.user.username, formattedCode));
		mailSender.send(message);
		logger.info("Successfully sent verification code to '{}'", user.user.id);
	}
}
