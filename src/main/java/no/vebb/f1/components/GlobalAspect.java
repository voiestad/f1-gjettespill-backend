package no.vebb.f1.components;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import no.vebb.f1.user.UserService;
import no.vebb.f1.exception.NoUsernameException;
import no.vebb.f1.exception.NotAdminException;

@Aspect
@Component
public class GlobalAspect {

	private final UserService userService;

    public GlobalAspect(UserService userService) {
        this.userService = userService;
    }

    @Before("execution(public * no.vebb.f1.controller.admin..*(..))")
    public void adminCheck() throws NotAdminException {
        userService.adminCheck();
    }

    @Before("execution(public * no.vebb.f1.controller..*(..)) && " +
            "!execution(public * no.vebb.f1.controller.open..*(..)) && " +
            "!execution(public * no.vebb.f1.controller.user.UserSettingsController.changeUsername(..))")
    public void usernameCheck() throws NoUsernameException {
        userService.usernameCheck();
    }
}
