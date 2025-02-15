package no.vebb.f1.components;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import no.vebb.f1.user.UserService;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NotAdminException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Autowired
	private UserService userService;

    @ExceptionHandler(InvalidYearException.class)
    public String handleInvalidYear(InvalidYearException e) {
        return "redirect:/admin/season";
    }
    
	@ExceptionHandler(NotAdminException.class)
    public String handleNotAdmin(NotAdminException e) {
		UUID userId = userService.loadUser().get().id;
		logger.warn("User '{}' tried accessing an admin page without the correct access rights", userId);
        return "redirect:/";
    }
}