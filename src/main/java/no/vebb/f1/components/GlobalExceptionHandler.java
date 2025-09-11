package no.vebb.f1.components;

import no.vebb.f1.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import no.vebb.f1.user.UserService;

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

    @ExceptionHandler(YearFinishedException.class)
    public ResponseEntity<?> handleYearFinished() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
