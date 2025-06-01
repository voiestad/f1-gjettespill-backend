package no.vebb.f1.controller.user;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.vebb.f1.util.response.ReferralCodeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserMail;
import no.vebb.f1.user.UserMailService;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.domainPrimitive.MailOption;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidEmailException;
import no.vebb.f1.util.exception.InvalidUsernameException;
import no.vebb.f1.util.response.MailOptionsResponse;

/**
 * Class is responsible for managing the user settings. Like changing username
 * and deleting user.
 */
@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);

    @Autowired
    private Database db;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMailService userMailService;


    /**
     * Handles GET requests for /settings/info. Gives the user information about
     * their username, user ID and Google ID that is associated with their user.
     */
    @GetMapping("/info")
    public ResponseEntity<UserInformation> userInformation() {
        User user = userService.loadUser().get();
        UserInformation userInfo = new UserInformation(user, db);
        return new ResponseEntity<>(userInfo, HttpStatus.OK);

    }

    /**
     * Handles POST requests for /settings/username. If the username is valid, it
     * changes the username in the database. Otherwise, it gives an error message to
     * the user.
     */
    @PostMapping("/username/change")
    @Transactional
    public ResponseEntity<String> changeUsername(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("username") String username,
            @RequestParam(value = "referralCode", required = false) Long referralCode) {
        try {
            Username validUsername = new Username(username, db);
            if (!userService.isLoggedIn()) {
                return registerUsername(principal, validUsername, referralCode);
            }
            final UUID id = userService.loadUser().get().id;
            db.updateUsername(validUsername, id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidUsernameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    private ResponseEntity<String> registerUsername(OAuth2User principal, Username username, Long referralCode) {
        if (referralCode != null && !db.isValidReferralCode(referralCode)) {
            logger.warn("Someone tried to use an invalid referral code.");
            logger.warn("{}", referralCode);
            return new ResponseEntity<>("Ikke gyldig invitasjonskode.", HttpStatus.BAD_REQUEST);
        }
        final String googleId = principal.getName();
        try {
            db.addUser(username, googleId);
        } catch (DataAccessException e) {
            // Try again to ensure it could not be equal UUID
            logger.warn("Failed to set UUID to new user. Tried again.");
            db.addUser(username, googleId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername() {
        String username = userService.loadUser().get().username;
        return new ResponseEntity<>(username, HttpStatus.OK);
    }

    /**
     * Handles POST requests for /settings/delete. If the input username matches the
     * username of the user the user is anonymized and Google ID removed. This
     * revokes their access to the website. If the username is incorrect, the user
     * gets an error message.
     */
    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteAccount(@RequestParam("username") String username, HttpServletRequest request) {
        User user = userService.loadUser().get();
        String actualUsername = user.username;
        if (!username.equals(actualUsername)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        db.deleteUser(user.id);

        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mail")
    public ResponseEntity<MailOptionsResponse> mailingList() {
        User user = userService.loadUser().get();
        boolean hasMail = db.userHasEmail(user.id);
        if (!hasMail) {
            MailOptionsResponse res = new MailOptionsResponse(false, null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        Map<Integer, Boolean> mailOptions = new LinkedHashMap<>();
        List<MailOption> options = db.getMailingOptions();
        for (MailOption option : options) {
            mailOptions.put(option.value, false);
        }
        List<MailOption> preferences = db.getMailingPreference(user.id);
        for (MailOption preference : preferences) {
            mailOptions.put(preference.value, true);
        }
        MailOptionsResponse res = new MailOptionsResponse(true, mailOptions);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @PostMapping("/mail/add")
    @Transactional
    public ResponseEntity<?> addMailingList(@RequestParam("email") String email) {
        try {
            User user = userService.loadUser().get();
            UserMail userMail = new UserMail(user, email);
            userMailService.sendVerificationCode(userMail);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/mail/remove")
    @Transactional
    public ResponseEntity<?> removeMailingList() {
        User user = userService.loadUser().get();
        db.clearUserFromMailing(user.id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mail/verification")
    public ResponseEntity<Boolean> hasVerificationCode() {
        User user = userService.loadUser().get();
        return new ResponseEntity<>(db.hasVerificationCode(user.id), HttpStatus.OK);
    }

    @PostMapping("/mail/verification")
    @Transactional
    public ResponseEntity<?> verificationCode(@RequestParam("code") int code) {
        User user = userService.loadUser().get();
        boolean isValidVerificationCode = db.isValidVerificationCode(user.id, code);
        if (isValidVerificationCode) {
            logger.info("Successfully verified email of user '{}'", user.id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        logger.warn("User '{}' put the wrong verification code", user.id);

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/mail/option/add")
    @Transactional
    public ResponseEntity<?> addMailingOption(@RequestParam("option") int option) {
        try {
            User user = userService.loadUser().get();
            MailOption mailOption = new MailOption(option, db);
            db.addMailOption(user.id, mailOption);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/mail/option/remove")
    @Transactional
    public ResponseEntity<?> removeMailingOption(@RequestParam("option") int option) {
        try {
            User user = userService.loadUser().get();
            MailOption mailOption = new MailOption(option, db);
            db.removeMailOption(user.id, mailOption);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/referral")
    public ResponseEntity<ReferralCodeResponse> getReferralCode() {
        UUID userId = userService.loadUser().get().id;
        Long referralCode = db.getReferralCode(userId);
        ReferralCodeResponse code = new ReferralCodeResponse(referralCode);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/add")
    @Transactional
    public ResponseEntity<ReferralCodeResponse> generateReferralCode() {
        UUID userId = userService.loadUser().get().id;
        ReferralCodeResponse code = new ReferralCodeResponse(db.addReferralCode(userId));
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/delete")
    @Transactional
    public ResponseEntity<?> removeReferralCode() {
        UUID userId = userService.loadUser().get().id;
        db.removeReferralCode(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
