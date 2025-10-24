package no.voiestad.f1.controller.open;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/public/csrf-token")
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
}
