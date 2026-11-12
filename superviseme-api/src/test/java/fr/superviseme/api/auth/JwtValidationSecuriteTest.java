package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JwtValidationSecuriteTest {

    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired UtilisateurRepository utilisateurs;

    private String creerToken(String issuer, List<String> audiences,
                              Instant expiration) {
        Utilisateur utilisateur = new Utilisateur(
                "jwt-test-" + UUID.randomUUID() + "@test.local",
                "hash-test",
                RoleUtilisateur.ADMIN
        );
        utilisateurs.saveAndFlush(utilisateur);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(utilisateur.getId().toString())
                .issuedAt(Instant.now().minusSeconds(10))
                .audience(audiences)
                .claim("roles", List.of("ADMIN"));

        if (expiration != null) {
            claims.expiresAt(expiration);
        }

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(),
                claims.build()
        )).getTokenValue();
    }

    @Test
    void jwtValideAccepte() {
        String token = creerToken(
                "superviseme",
                List.of("superviseme-api"),
                Instant.now().plusSeconds(1800)
        );

        assertNotNull(decoder.decode(token));
    }

    @Test
    void issuerIncorrectRefuse() {
        String token = creerToken(
                "autre-application",
                List.of("superviseme-api"),
                Instant.now().plusSeconds(1800)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void audienceIncorrecteRefusee() {
        String token = creerToken(
                "superviseme",
                List.of("autre-entreprise"),
                Instant.now().plusSeconds(1800)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void expirationDepasseeRefusee() {
        String token = creerToken(
                "superviseme",
                List.of("superviseme-api"),
                Instant.now().minusSeconds(120)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void expirationAbsenteRefusee() {
        String token = creerToken(
                "superviseme",
                List.of("superviseme-api"),
                null
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }
}
