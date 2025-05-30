package no.vebb.f1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Class is responsible for configuring the security of the application.
 * It defines which pages the user can access without being authenticated, all
 * other pages require authentication. It also sets up oauth2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(auth -> {
			auth.requestMatchers(
				"/",
				"/favicon.ico",
				"/logo.svg",
				"/**.css",
				"/user/**",
				"/score/**",
				"/race-guess/**",
				"/contact",
				"/about",
				"/error",
				"/privacy",
				"/stats/**",
				"/bingo",
				"/api/public/**")
				.permitAll();
			auth.anyRequest().authenticated();
		})
		.exceptionHandling(exception ->
			exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
		)
		.oauth2Login(o ->
			o.defaultSuccessUrl("/logged-in", true)
		)
		.logout(logout -> logout.logoutSuccessUrl("/"))
		.build();
	}


}
