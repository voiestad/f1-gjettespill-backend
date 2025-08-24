package no.vebb.f1.user;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import no.vebb.f1.database.Database;
import no.vebb.f1.util.CodeGenerator;

@Service
public class UserMailService {

	private static final Logger logger = LoggerFactory.getLogger(UserMailService.class);
	private final JavaMailSender mailSender;
	private final Database db;
	private final UserRespository userRespository;
	private final UserService userService;

	public UserMailService(JavaMailSender mailSender, Database db, UserRespository userRespository, UserService userService) {
		this.mailSender = mailSender;
		this.db = db;
		this.userRespository = userRespository;
		this.userService = userService;
	}

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendVerificationCode(UserMail user) {
		final int verificationCode = CodeGenerator.getVerificationCode();
		db.addVerificationCode(user, verificationCode);
		String strCode = String.valueOf(verificationCode);
		String formattedCode = String.format("%s %s %s",
				strCode.substring(0, 3), strCode.substring(3, 6), strCode.substring(6, 9));
		try {
			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromEmail, "F1 Tipping"));
			message.addRecipients(RecipientType.TO, user.email());
			message.setSubject("Verifikasjonskode F1 Tipping");
			message.setContent(String.format("Hei %s!\n\nHer er din verifikasjonskode: %s\n\nDen er gyldig i 10 minutter.",
					user.user().username(), formattedCode), "text/plain; charset=UTF-8");
			mailSender.send(message);
			logger.info("Successfully sent verification code to '{}'", user.user().id());
		} catch (MessagingException | UnsupportedEncodingException ignored) {
		}
    }

	public void sendServerMessageToAdmins(String messageForAdmin) {
		List<UUID> admins = userService.getAdmins();
		List<UserMail> adminsWithMail = admins.stream()
			.filter(db::userHasEmail)
			.map(userRespository::findById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(admin -> new UserMail(admin, db.getEmail(admin.id())))
			.toList();
		for (UserMail admin : adminsWithMail) {
			try {
				MimeMessage message = mailSender.createMimeMessage();
				message.setFrom(new InternetAddress(fromEmail, "F1 Tipping"));
				message.addRecipients(RecipientType.TO, admin.email());
				message.setSubject("Server melding F1 Tipping");
				message.setContent(String.format("Hei administrator!\n\nDette er en automatisk generert melding:\n%s",
						messageForAdmin), "text/plain; charset=UTF-8");
				mailSender.send(message);
				logger.info("Successfully sent server message to '{}'", admin.user().id());
			} catch (MessagingException | UnsupportedEncodingException ignored) {
			}
        }
	}
}
