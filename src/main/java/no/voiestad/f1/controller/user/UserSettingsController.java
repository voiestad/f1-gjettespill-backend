package no.voiestad.f1.controller.user;

import java.util.*;

import no.voiestad.f1.codes.CodeService;
import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.notification.NotificationService;
import no.voiestad.f1.response.ReferralCodeResponse;
import no.voiestad.f1.user.UserRespository;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.notification.guessReminderOption.GuessReminderOption;
import no.voiestad.f1.user.domain.Username;
import no.voiestad.f1.exception.InvalidUsernameException;
import no.voiestad.f1.response.GuessReminderOptionsResponse;

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

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);
    private final UserService userService;
    private final UserRespository userRespository;
    private final CodeService codeService;
    private final NotificationService notificationService;
    private final GuessService guessService;

    public UserSettingsController(
            UserService userService,
            UserRespository userRespository,
            CodeService codeService,
            NotificationService notificationService,
            GuessService guessService) {
        this.userService = userService;
        this.userRespository = userRespository;
        this.codeService = codeService;
        this.notificationService = notificationService;
        this.guessService = guessService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInformation> userInformation() {
        UserInformation userInfo = new UserInformation(userService.getUser(), notificationService, guessService);
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
        if (referralCode != null && codeService.isValidReferralCode(referralCode)) {
            userService.addUser(username, principal);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        logger.warn("Someone tried to use an invalid referral code.");
        logger.warn("{}", referralCode);
        return new ResponseEntity<>("Ikke gyldig invitasjonskode.", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername() {
        String username = userService.getUser().username();
        return new ResponseEntity<>(username, HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteAccount(@RequestParam("username") String username) {
        String actualUsername = userService.getUser().username();
        if (username.equals(actualUsername)) {
            userService.deleteUser();
            SecurityContextHolder.clearContext();
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/ntfy")
    public ResponseEntity<GuessReminderOptionsResponse> ntfyOverview() {
        UUID userID = userService.getUser().id();
        Optional<UUID> optTopic = notificationService.getNtfyTopic(userID);
        if (optTopic.isEmpty()) {
            GuessReminderOptionsResponse res = new GuessReminderOptionsResponse(null, null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        Map<GuessReminderOption, Boolean> guessReminderOptions = new LinkedHashMap<>();
        List<GuessReminderOption> options = notificationService.getGuessReminderOptions();
        for (GuessReminderOption option : options) {
            guessReminderOptions.put(option, false);
        }
        List<GuessReminderOption> preferences = notificationService.getGuessReminderPreference(userID);
        for (GuessReminderOption preference : preferences) {
            guessReminderOptions.put(preference, true);
        }
        GuessReminderOptionsResponse res = new GuessReminderOptionsResponse(optTopic.get(), guessReminderOptions);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @PostMapping("/ntfy/add")
    @Transactional
    public ResponseEntity<UUID> addNtfy() {
        return new ResponseEntity<>(notificationService.addNtfyTopic(userService.getUser().id()), HttpStatus.OK);
    }

    @PostMapping("/ntfy/remove")
    @Transactional
    public ResponseEntity<?> removeNtfy() {
        notificationService.clearUserFromNtfy(userService.getUser().id());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/option/add")
    @Transactional
    public ResponseEntity<?> addGuessReminderOption(@RequestParam("option") int option) {
        Optional<GuessReminderOption> guessReminderOption = notificationService.getGuessReminderOption(option);
        if (guessReminderOption.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        notificationService.addGuessReminderOption(userService.getUser().id(), guessReminderOption.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/option/remove")
    @Transactional
    public ResponseEntity<?> removeGuessReminderOption(@RequestParam("option") int option) {
        Optional<GuessReminderOption> guessReminderOption = notificationService.getGuessReminderOption(option);
        if (guessReminderOption.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        notificationService.removeGuessReminderOption(userService.getUser().id(), guessReminderOption.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/test")
    public ResponseEntity<?> testNotification() {
        if (notificationService.testNotification(userService.getUser().id())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/referral")
    public ResponseEntity<ReferralCodeResponse> getReferralCode() {
        Long referralCode = codeService.getReferralCode(userService.getUser().id());
        ReferralCodeResponse code = new ReferralCodeResponse(referralCode);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/add")
    @Transactional
    public ResponseEntity<ReferralCodeResponse> generateReferralCode() {
        ReferralCodeResponse code = new ReferralCodeResponse(codeService.addReferralCode(userService.getUser().id()));
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PostMapping("/referral/delete")
    @Transactional
    public ResponseEntity<?> removeReferralCode() {
        codeService.removeReferralCode(userService.getUser().id());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
