package no.vebb.f1.controller.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidUsernameException;

/**
 * Class is responsible for registering username for new users.
 */
@RestController
@RequestMapping("/api/username")
public class UsernameController {

	private static final Logger logger = LoggerFactory.getLogger(UsernameController.class);

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	/**
	 * Handles POST requests for /username. If the username is valid, it adds the
	 * username along with the users Google ID and a generated ID into the database.
	 * Otherwise, it gives the user an error message.
	 */
	@PostMapping
	@Transactional
	public ResponseEntity<String> registerUsername(@AuthenticationPrincipal OAuth2User principal,
			@RequestParam("username") String username,
			@RequestParam("referralCode") long referralCode) {
		if (userService.isLoggedIn()) {
			return new ResponseEntity<>("Brukernavn er allerede satt.`", HttpStatus.FORBIDDEN);
		}
		if (!db.isValidReferralCode(referralCode)) {
			logger.warn("Someone tried to use an invalid referral code.");
			return new ResponseEntity<>("Ikke gyldig invitasjonskode.", HttpStatus.BAD_REQUEST);
		}
		try {
			final String googleId = principal.getName();
			Username validUsername = new Username(username, db);
			try {
				db.addUser(validUsername, googleId);
			} catch (DataAccessException e) {
				// Try again to ensure it could not be equal UUID
				logger.warn("Failed to set UUID to new user. Tried again.");
				db.addUser(validUsername, googleId);
			}
		} catch (InvalidUsernameException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
