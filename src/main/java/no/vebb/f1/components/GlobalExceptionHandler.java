package no.vebb.f1.components;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import no.vebb.f1.util.InvalidYearException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidYearException.class)
    public String handleInvalidYear(InvalidYearException e) {
        return "redirect:/admin/season";
    }
}