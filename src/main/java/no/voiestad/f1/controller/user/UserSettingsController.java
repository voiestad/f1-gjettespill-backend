package no.voiestad.f1.controller.user;

import java.io.IOException;
import java.util.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.notification.NotificationService;
import no.voiestad.f1.user.UserRespository;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.notification.guessReminderOption.GuessReminderOption;
import no.voiestad.f1.user.domain.Username;
import no.voiestad.f1.exception.InvalidUsernameException;
import no.voiestad.f1.response.GuessReminderOptionsResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserService userService;
    private final UserRespository userRespository;
    private final NotificationService notificationService;
    private final GuessService guessService;

    public UserSettingsController(
            UserService userService,
            UserRespository userRespository,
            NotificationService notificationService,
            GuessService guessService) {
        this.userService = userService;
        this.userRespository = userRespository;
        this.notificationService = notificationService;
        this.guessService = guessService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInformation> userInformation() {
        UserInformation userInfo = new UserInformation(userService.getUser(), userService.getProviders(), notificationService, guessService);
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @PostMapping("/username/change")
    @Transactional
    public ResponseEntity<String> changeUsername(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client,
            @RequestParam("username") String username) {
        try {
            Username validUsername = new Username(username, userRespository);
            if (userService.isLoggedIn()) {
                userService.changeUsername(validUsername);
            } else {
                userService.addUser(validUsername, client);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidUsernameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
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

    @Transactional
    @GetMapping("/link")
    public void linkLogin(HttpServletRequest request, HttpServletResponse response,
                          @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) throws IOException {
        Cookie[] cookies = request.getCookies();
        String linkCode = Arrays.stream(cookies != null ? cookies : new Cookie[0])
                .filter(c -> "LINK_CODE".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
        if (linkCode == null) {
            response.sendRedirect("/settings/account?error=no_cookie");
            return;
        }
        try {
            if (userService.addProvider(linkCode, client)) {
                Cookie cookie = new Cookie("LINK_CODE", null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                response.sendRedirect("/settings/account");
            } else {
                response.sendRedirect("/settings/account?error=unknown");
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/settings/account?error=parsing");
        }
    }

    @Transactional
    @PostMapping("/link")
    public void createUserLink(HttpServletResponse response) {
        UUID userId = userService.getUser().id();
        String code = userService.addUserLinking(userId);
        Cookie codeCookie = new Cookie("LINK_CODE", code);
        codeCookie.setHttpOnly(true);
        codeCookie.setSecure(true);
        codeCookie.setPath("/");
        codeCookie.setMaxAge(60 * 10);
        response.addCookie(codeCookie);
    }

    @Transactional
    @PostMapping("/unlink/{provider}")
    public ResponseEntity<?> unlinkAccount(@PathVariable String provider) {
        Map<String, String> providers = userService.getProviders();
        if (providers.size() == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (providers.containsKey(provider)) {
            userService.deleteProvider(provider);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
