package no.vebb.f1.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import no.vebb.f1.user.UserService;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoUsernameException;
import no.vebb.f1.util.exception.NotAdminException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final UserService userService;

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(InvalidYearException.class)
    public ResponseEntity<?> handleInvalidYear(InvalidYearException e) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAdminException.class)
    public ResponseEntity<?> handleNotAdmin(NotAdminException e) {
        userService.loadUser().ifPresentOrElse(
                user -> logger.warn("User '{}' tried accessing an admin page without the correct access rights",
                        user.id),
                () -> logger.warn("Someone tried accessing an admin page without the correct access rights"));
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoUsernameException.class)
    public ResponseEntity<?> handleNoUsername(NoUsernameException e) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
