package no.voiestad.f1.components;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import no.voiestad.f1.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretGenerator {

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.private-key}")
    private String privateKeyPem;

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String clientId;

    public String generateClientSecret() {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + TimeUtil.MONTH * 4);

            return Jwts.builder()
                    .header().add("kid", keyId).and()
                    .issuer(teamId)
                    .subject(clientId)
                    .audience().add("https://appleid.apple.com").and()
                    .issuedAt(now)
                    .expiration(expiration)
                    .signWith(getPrivateKey(), SIG.ES256)
                    .compact();
        } catch (Exception e) {
            return "";
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        String key = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }
}
