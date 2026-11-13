package fr.superviseme.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import java.util.ArrayList;
import java.util.List;
import java.security.interfaces.RSAPrivateCrtKey;
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
            @Value("${superviseme.jwt.public-key}") String publicKey,
            @Value("${superviseme.jwt.active-key-id:superviseme-1}") String activeKeyId
    ) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        RSAPrivateKey privateRsa = (RSAPrivateKey) factory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

        RSAPublicKey publicRsa = (RSAPublicKey) factory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));

        if (!(privateRsa instanceof RSAPrivateCrtKey crt)
                || !crt.getModulus().equals(publicRsa.getModulus())) {
            throw new IllegalStateException("Paire RSA incohérente");
        }
        if (activeKeyId.isBlank()) {
            throw new IllegalStateException("Identifiant RSA actif absent");
        }

        return new RSAKey.Builder(publicRsa)
                .privateKey(privateRsa)
                .keyID(activeKeyId)
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
            UtilisateurRepository utilisateurs,
            JwtRevocationService revocations,
            @Value("${superviseme.jwt.issuer:superviseme}") String issuer,
            @Value("${superviseme.jwt.audience:superviseme-api}") String audience,
            @Value("${superviseme.jwt.previous-public-key:}") String previousPublicKey,
            @Value("${superviseme.jwt.previous-key-id:}") String previousKeyId
    ) throws Exception {
        List<com.nimbusds.jose.jwk.JWK> verificationKeys = new ArrayList<>();
        verificationKeys.add(new RSAKey.Builder(rsaKey.toRSAPublicKey())
                .keyID(rsaKey.getKeyID())
                .build());

        if (!previousPublicKey.isBlank()) {
            if (previousKeyId.isBlank()
                    || previousKeyId.equals(rsaKey.getKeyID())) {
                throw new IllegalStateException("Identifiant RSA précédent invalide");
            }

            RSAPublicKey previous = (RSAPublicKey) KeyFactory
                    .getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(
                            Base64.getDecoder().decode(previousPublicKey)));

            verificationKeys.add(new RSAKey.Builder(previous)
                    .keyID(previousKeyId)
                    .build());
        } else if (!previousKeyId.isBlank()) {
            throw new IllegalStateException("Clé RSA précédente absente");
        }

        var processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(
                        JWSAlgorithm.RS256,
                        new ImmutableJWKSet<>(
                                new JWKSet(verificationKeys))
                )
        );

        NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);

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

        // Refuse une audience absente ou étrangère à cette instance.
        OAuth2TokenValidator<Jwt> audienceValide = token ->
                token.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Audience invalide", null));

        // L'expiration doit être explicitement présente.
        OAuth2TokenValidator<Jwt> expirationPresente = token ->
                token.getExpiresAt() != null
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Expiration absente", null));

        OAuth2TokenValidator<Jwt> nonRevoque = jwt ->
                !revocations.estRevoque(jwt.getTokenValue())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token",
                                        "JWT révoqué", null));

        // Validation de l'émetteur, de l'expiration et du contexte.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                audienceValide,
                expirationPresente,
                compteActif,
                nonRevoque
        ));

        return decoder;
    }
}
