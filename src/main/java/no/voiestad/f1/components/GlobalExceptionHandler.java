package no.voiestad.f1.components;

import no.voiestad.f1.exception.*;
import no.voiestad.f1.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final UserService userService;

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(NotAdminException.class)
    public ResponseEntity<?> handleNotAdmin() {
        userService.loadUser().ifPresentOrElse(
                user -> logger.warn("User '{}' tried accessing an admin page without the correct access rights",
                        user.id()),
                () -> logger.warn("Someone tried accessing an admin page without the correct access rights"));
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoUsernameException.class)
    public ResponseEntity<?> handleNoUsername() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<String> handleConversionFailed() {
        return new ResponseEntity<>("Invalid inputted value", HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DomainConversionException.class)
    public ResponseEntity<String> handleDomainConversion() {
        return new ResponseEntity<>("Invalid inputted value", HttpStatus.BAD_REQUEST);
    }
}
