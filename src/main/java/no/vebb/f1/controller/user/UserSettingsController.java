package no.vebb.f1.controller.user;

import java.util.*;

import no.vebb.f1.codes.CodeService;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.notification.NotificationService;
import no.vebb.f1.user.*;
import no.vebb.f1.response.ReferralCodeResponse;
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

import no.vebb.f1.notification.guessReminderOption.GuessReminderOption;
import no.vebb.f1.user.domain.Username;
import no.vebb.f1.exception.InvalidUsernameException;
import no.vebb.f1.response.GuessReminderOptionsResponse;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);
    private final UserService userService;
    private final UserRespository userRespository;
    private final CodeService codeService;
    private final NotificationService notificationService;
    private final GuessService guessService;

    public UserSettingsController(UserService userService, UserRespository userRespository, CodeService codeService, NotificationService notificationService, GuessService guessService) {
        this.userService = userService;
        this.userRespository = userRespository;
        this.codeService = codeService;
        this.notificationService = notificationService;
        this.guessService = guessService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInformation> userInformation() {
        UserEntity userEntity = userService.getUser();
        UserInformation userInfo = new UserInformation(userEntity, notificationService, guessService);
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

    @GetMapping("/ntfy")
    public ResponseEntity<GuessReminderOptionsResponse> ntfyOverview() {
        UserEntity userEntity = userService.getUser();
        Optional<UUID> optTopic = notificationService.getNtfyTopic(userEntity.id());
        if (optTopic.isEmpty()) {
            GuessReminderOptionsResponse res = new GuessReminderOptionsResponse(null, null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        Map<GuessReminderOption, Boolean> guessReminderOptions = new LinkedHashMap<>();
        List<GuessReminderOption> options = notificationService.getGuessReminderOptions();
        for (GuessReminderOption option : options) {
            guessReminderOptions.put(option, false);
        }
        List<GuessReminderOption> preferences = notificationService.getGuessReminderPreference(userEntity.id());
        for (GuessReminderOption preference : preferences) {
            guessReminderOptions.put(preference, true);
        }
        GuessReminderOptionsResponse res = new GuessReminderOptionsResponse(optTopic.get(), guessReminderOptions);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @PostMapping("/ntfy/add")
    @Transactional
    public ResponseEntity<UUID> addNtfy() {
        UserEntity userEntity = userService.getUser();
        return new ResponseEntity<>(notificationService.addNtfyTopic(userEntity.id()), HttpStatus.OK);
    }

    @PostMapping("/ntfy/remove")
    @Transactional
    public ResponseEntity<?> removeNtfy() {
        UserEntity userEntity = userService.getUser();
        notificationService.clearUserFromNtfy(userEntity.id());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/option/add")
    @Transactional
    public ResponseEntity<?> addGuessReminderOption(@RequestParam("option") int option) {
        UserEntity userEntity = userService.getUser();
        Optional<GuessReminderOption> guessReminderOption = notificationService.getGuessReminderOption(option);
        if (guessReminderOption.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        notificationService.addGuessReminderOption(userEntity.id(), guessReminderOption.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/option/remove")
    @Transactional
    public ResponseEntity<?> removeGuessReminderOption(@RequestParam("option") int option) {
        UserEntity userEntity = userService.getUser();
        Optional<GuessReminderOption> guessReminderOption = notificationService.getGuessReminderOption(option);
        if (guessReminderOption.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        notificationService.removeGuessReminderOption(userEntity.id(), guessReminderOption.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ntfy/test")
    public ResponseEntity<?> testNotification() {
        UserEntity user = userService.getUser();
        if (notificationService.testNotification(user.id())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
