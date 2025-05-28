package no.vebb.f1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Class is responsible for configuring the security of the application.
 * It defines which pages the user can access without being authenticated, all
 * other pages require authentication. It also sets up oauth2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${f1.login.success-redirect-uri}")
	private String loginRedirectUri;
	@Value("${f1.logout.success-redirect-uri}")
	private String logoutRedirectUri;

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
		.oauth2Login(o ->
			o.defaultSuccessUrl(loginRedirectUri, true)
		)
		.logout(logout -> logout.logoutSuccessUrl(logoutRedirectUri))
		.build();
	}


}
