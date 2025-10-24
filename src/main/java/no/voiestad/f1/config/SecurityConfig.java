package no.voiestad.f1.config;

import no.voiestad.f1.components.F1OAuth2AuthorizationRequestResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Class is responsible for configuring the security of the application.
 * It defines which end points the user can access without being authenticated, all
 * other pages require authentication. It also sets up oauth2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository
    ) throws Exception {
        return http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/api/public/**").permitAll();
            auth.anyRequest().authenticated();
        })
        .exceptionHandling(exception ->
            exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        )
        .oauth2Login(o -> {
            o.authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint.authorizationRequestResolver(
                    new F1OAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                    )
                )
            );
            o.defaultSuccessUrl("/logged-in", true);
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
