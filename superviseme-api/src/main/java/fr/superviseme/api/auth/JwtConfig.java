package fr.superviseme.api.auth;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

/** Configuration des JWT signés avec RSA. */
@Configuration
public class JwtConfig {

    @Bean
    RSAKey rsaKey(
            @Value("${superviseme.jwt.private-key}") String privateKey,
            @Value("${superviseme.jwt.public-key}") String publicKey
    ) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        RSAPrivateKey privateRsa = (RSAPrivateKey) factory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

        RSAPublicKey publicRsa = (RSAPublicKey) factory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));

        return new RSAKey.Builder(publicRsa)
                .privateKey(privateRsa)
                .keyID("superviseme-1")
                .build();
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey))
        );
    }

    @Bean
    JwtDecoder jwtDecoder(
            RSAKey rsaKey,
            UtilisateurRepository utilisateurs
    ) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();

        /*
         * Un JWT correctement signé ne suffit pas :
         * le compte doit toujours exister et être actif.
         * Ce contrôle invalide immédiatement les anciens JWT.
         */
        OAuth2TokenValidator<Jwt> compteActif = jwt -> {
            try {
                UUID id = UUID.fromString(jwt.getSubject());

                boolean actif = utilisateurs.findById(id)
                        .map(Utilisateur::isActif)
                        .orElse(false);

                if (actif) {
                    return OAuth2TokenValidatorResult.success();
                }
            } catch (IllegalArgumentException exception) {
                // Un identifiant JWT invalide ne donne aucun accès.
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Compte inactif", null)
            );
        };

        // Conserve également les vérifications standard d'expiration.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                compteActif
        ));

        return decoder;
    }
}
