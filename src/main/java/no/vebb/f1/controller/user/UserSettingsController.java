package no.vebb.f1.controller.user;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.vebb.f1.codes.CodeService;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.mail.Email;
import no.vebb.f1.mail.MailService;
import no.vebb.f1.user.*;
import no.vebb.f1.util.response.ReferralCodeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import no.vebb.f1.mail.mailOption.MailOption;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidEmailException;
import no.vebb.f1.util.exception.InvalidUsernameException;
import no.vebb.f1.util.response.MailOptionsResponse;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);
    private final UserService userService;
    private final UserRespository userRespository;
    private final CodeService codeService;
    private final MailService mailService;
    private final GuessService guessService;

    public UserSettingsController(UserService userService, UserRespository userRespository, CodeService codeService, MailService mailService, GuessService guessService) {
        this.userService = userService;
        this.userRespository = userRespository;
        this.codeService = codeService;
        this.mailService = mailService;
        this.guessService = guessService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInformation> userInformation() {
        UserEntity userEntity = userService.getUser();
        UserInformation userInfo = new UserInformation(userEntity, mailService, guessService);
        return new ResponseEntity<>(userInfo, HttpStatus.OK);

    }

    @PostMapping("/username/change")
    @Transactional
    public ResponseEntity<String> changeUsername(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("username") String username,
            @RequestParam(value = "referralCode", required = false) Long referralCode) {
        try {
            Username validUsername = new Username(username, userRespository);
            if (!userService.isLoggedIn()) {
                return registerUsername(principal, validUsername, referralCode);
            }
            userService.changeUsername(validUsername);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidUsernameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    private ResponseEntity<String> registerUsername(OAuth2User principal, Username username, Long referralCode) {
        if (referralCode != null && !codeService.isValidReferralCode(referralCode)) {
            logger.warn("Someone tried to use an invalid referral code.");
            logger.warn("{}", referralCode);
            return new ResponseEntity<>("Ikke gyldig invitasjonskode.", HttpStatus.BAD_REQUEST);
        }
        userService.addUser(username, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername() {
        String username = userService.getUser().username();
        return new ResponseEntity<>(username, HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteAccount(@RequestParam("username") String username) {
        UserEntity userEntity = userService.getUser();
        String actualUsername = userEntity.username();
        if (!username.equals(actualUsername)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userService.deleteUser();
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mail")
    public ResponseEntity<MailOptionsResponse> mailingList() {
        UserEntity userEntity = userService.getUser();
        boolean hasMail = mailService.userHasEmail(userEntity.id());
        if (!hasMail) {
            MailOptionsResponse res = new MailOptionsResponse(false, null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        Map<Integer, Boolean> mailOptions = new LinkedHashMap<>();
        List<MailOption> options = mailService.getMailingOptions();
        for (MailOption option : options) {
            mailOptions.put(option.value, false);
        }
        List<MailOption> preferences = mailService.getMailingPreference(userEntity.id());
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
            UserEntity userEntity = userService.getUser();
            UserMail userMail = new UserMail(userEntity, new Email(email));
            codeService.sendVerificationCode(userMail);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/mail/remove")
    @Transactional
    public ResponseEntity<?> removeMailingList() {
        UserEntity userEntity = userService.getUser();
        mailService.clearUserFromMailing(userEntity.id());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mail/verification")
    public ResponseEntity<Boolean> hasVerificationCode() {
        UserEntity userEntity = userService.getUser();
        return new ResponseEntity<>(codeService.hasVerificationCode(userEntity.id()), HttpStatus.OK);
    }

    @PostMapping("/mail/verification")
    @Transactional
    public ResponseEntity<?> verificationCode(@RequestParam("code") int code) {
        UserEntity userEntity = userService.getUser();
        boolean isValidVerificationCode = codeService.validateVerificationCode(userEntity.id(), code);
        if (isValidVerificationCode) {
            logger.info("Successfully verified email of user '{}'", userEntity.id());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        logger.warn("User '{}' put the wrong verification code", userEntity.id());

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/mail/option/add")
    @Transactional
    public ResponseEntity<?> addMailingOption(@RequestParam("option") int option) {
        try {
            UserEntity userEntity = userService.getUser();
            MailOption mailOption = mailService.getMailOption(option);
            mailService.addMailOption(userEntity.id(), mailOption);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/mail/option/remove")
    @Transactional
    public ResponseEntity<?> removeMailingOption(@RequestParam("option") int option) {
        try {
            UserEntity userEntity = userService.getUser();
            MailOption mailOption = mailService.getMailOption(option);
            mailService.removeMailOption(userEntity.id(), mailOption);
        } catch (InvalidEmailException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/referral")
    public ResponseEntity<ReferralCodeResponse> getReferralCode() {
        UUID userId = userService.getUser().id();
        Long referralCode = codeService.getReferralCode(userId);
        ReferralCodeResponse code = new ReferralCodeResponse(referralCode);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/add")
    @Transactional
    public ResponseEntity<ReferralCodeResponse> generateReferralCode() {
        UUID userId = userService.getUser().id();
        ReferralCodeResponse code = new ReferralCodeResponse(codeService.addReferralCode(userId));
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/delete")
    @Transactional
    public ResponseEntity<?> removeReferralCode() {
        UUID userId = userService.getUser().id();
        codeService.removeReferralCode(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
