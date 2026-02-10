package no.voiestad.f1.config;

import no.voiestad.f1.components.AppleClientSecretGenerator;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuth2ClientConfig {

    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final OAuth2ClientProperties oAuth2ClientProperties;

    public OAuth2ClientConfig(AppleClientSecretGenerator generator, OAuth2ClientProperties properties) {
        this.appleClientSecretGenerator = generator;
        this.oAuth2ClientProperties = properties;
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            DataSource dataSource,
            ClientRegistrationRepository clientRegistrationRepository) {

        JdbcOperations jdbcOperations = new JdbcTemplate(dataSource);
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = oAuth2ClientProperties.getRegistration().entrySet().stream()
                .map(entry -> {
                    if ("apple".equals(entry.getKey())) {
                        return createAppleClientRegistration(entry.getValue());
                    }
                    OAuth2ClientProperties.Registration props = entry.getValue();
                    return CommonOAuth2Provider.GOOGLE.getBuilder(entry.getKey())
                            .clientId(props.getClientId())
                            .clientSecret(props.getClientSecret())
                            .redirectUri(props.getRedirectUri())
                            .scope(props.getScope())
                            .build();
                })
                .toList();
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration createAppleClientRegistration(OAuth2ClientProperties.Registration props) {
        return ClientRegistration.withRegistrationId("apple")
                .clientId(props.getClientId())
                .clientSecret(appleClientSecretGenerator.generateClientSecret())
                .clientAuthenticationMethod(new ClientAuthenticationMethod(props.getClientAuthenticationMethod()))
                .authorizationGrantType(new AuthorizationGrantType(props.getAuthorizationGrantType()))
                .redirectUri(props.getRedirectUri())
                .scope(props.getScope())
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .clientName(props.getClientName())
                .build();
    }
}
