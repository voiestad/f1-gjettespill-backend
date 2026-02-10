package no.voiestad.f1.components;

import no.voiestad.f1.user.UserService;
import no.voiestad.f1.exception.NoUsernameException;
import no.voiestad.f1.exception.NotAdminException;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GlobalAspect {

	private final UserService userService;

    public GlobalAspect(UserService userService) {
        this.userService = userService;
    }

    @Before("execution(public * no.voiestad.f1.controller.admin..*(..))")
    public void adminCheck() throws NotAdminException {
        userService.adminCheck();
    }

    @Before("execution(public * no.voiestad.f1.controller..*(..)) && " +
            "!execution(public * no.voiestad.f1.controller.open..*(..)) && " +
            "!execution(public * no.voiestad.f1.controller.user.UserSettingsController.changeUsername(..)) && " +
            "!execution(public * no.voiestad.f1.controller.user.UserSettingsController.linkLogin(..))")
    public void usernameCheck() throws NoUsernameException {
        userService.usernameCheck();
    }
}
