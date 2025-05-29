package no.vebb.f1.components;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.vebb.f1.user.UserService;
import no.vebb.f1.util.exception.NoUsernameException;
import no.vebb.f1.util.exception.NotAdminException;

@Aspect
@Component
public class GlobalAspect {

	@Autowired
	private UserService userService;

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
