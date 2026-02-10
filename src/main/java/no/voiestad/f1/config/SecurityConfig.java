package no.voiestad.f1.config;

import jakarta.servlet.http.Cookie;
import no.voiestad.f1.components.AppleOAuth2UserService;
import no.voiestad.f1.components.F1OAuth2AuthorizationRequestResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.Arrays;

/**
 * Class is responsible for configuring the security of the application.
 * It defines which end points the user can access without being authenticated, all
 * other pages require authentication. It also sets up oauth2.
 */
@Configuration
@EnableWebSecurity
@EnableAsync
public class SecurityConfig {

    private final AppleOAuth2UserService appleOAuth2UserService;

    public SecurityConfig(AppleOAuth2UserService appleOAuth2UserService) {
        this.appleOAuth2UserService = appleOAuth2UserService;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            if ("apple".equals(registrationId)) {
                return appleOAuth2UserService.loadUser(userRequest);
            }
            return new DefaultOAuth2UserService().loadUser(userRequest);
        };
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository
    ) throws Exception {
        return http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/api/public/**").permitAll();
            auth.anyRequest().authenticated();
        })
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/login/oauth2/code/**")
        )
        .exceptionHandling(exception ->
            exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        )
        .oauth2Login(o -> {
            o.authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint.authorizationRequestResolver(
                    new F1OAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/api" + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                    )
                )
            );
            o.successHandler((request, response, authentication) -> {
                Cookie[] cookies = request.getCookies();
                boolean hasLinkCode = Arrays.stream(cookies != null ? cookies : new Cookie[0])
                        .filter(c -> "LINK_CODE".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst().isPresent();
                if (hasLinkCode) {
                    response.sendRedirect("/api/settings/link");
                } else {
                    response.sendRedirect("/logged-in");
                }
            });
            o.userInfoEndpoint(userInfo -> userInfo.userService(delegatingOAuth2UserService()));
        }
        )
        .logout(logout ->
            logout.logoutUrl("/api/logout")
            .logoutSuccessHandler(
            (request,
             response,
             authentication) ->
                 response.setStatus(HttpStatus.NO_CONTENT.value())
            )
        )
        .build();
    }


}
