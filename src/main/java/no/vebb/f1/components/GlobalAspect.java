package no.vebb.f1.components;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.vebb.f1.user.UserService;
import no.vebb.f1.util.exception.NotAdminException;

@Aspect
@Component
public class GlobalAspect {

	@Autowired
	private UserService userService;

    @Before("execution(* no.vebb.f1.controller.admin..*(..))")
    public void adminCheck() throws NotAdminException {
        userService.adminCheck();
    }
}