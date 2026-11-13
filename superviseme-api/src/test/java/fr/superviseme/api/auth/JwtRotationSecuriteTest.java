package fr.superviseme.api.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtRotationSecuriteTest {

    private KeyPair paire() throws Exception {
        KeyPairGenerator generateur = KeyPairGenerator.getInstance("RSA");
        generateur.initialize(2048);
        return generateur.generateKeyPair();
    }

    private String token(KeyPair paire, String kid, UUID sujet) {
        RSAKey cle = new RSAKey.Builder(
                (RSAPublicKey) paire.getPublic())
                .privateKey((RSAPrivateKey) paire.getPrivate())
                .keyID(kid)
                .build();

        JwtEncoder encodeur = new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(cle)));

        Instant maintenant = Instant.now();

        return encodeur.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256)
                        .keyId(kid).build(),
                JwtClaimsSet.builder()
                        .issuer("superviseme")
                        .audience(List.of("superviseme-api"))
                        .subject(sujet.toString())
                        .issuedAt(maintenant)
                        .expiresAt(maintenant.plusSeconds(1800))
                        .claim("roles", List.of("ADMIN"))
                        .build()
        )).getTokenValue();
    }

    @Test
    void rotationAccepteDeuxClesEtRefuseCleEtrangere() throws Exception {
        KeyPair ancienne = paire();
        KeyPair nouvelle = paire();
        KeyPair etrangere = paire();

        JwtConfig config = new JwtConfig();
        RSAKey active = config.rsaKey(
                Base64.getEncoder().encodeToString(
                        nouvelle.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(
                        nouvelle.getPublic().getEncoded()),
                "rsa-nouvelle"
        );

        UtilisateurRepository utilisateurs =
                mock(UtilisateurRepository.class);
        JwtRevocationService revocations =
                mock(JwtRevocationService.class);

        UUID identifiant = UUID.randomUUID();
        Utilisateur utilisateur = mock(Utilisateur.class);
        when(utilisateur.isActif()).thenReturn(true);
        when(utilisateurs.findById(identifiant))
                .thenReturn(Optional.of(utilisateur));

        JwtDecoder decoder = config.jwtDecoder(
                active,
                utilisateurs,
                revocations,
                "superviseme",
                "superviseme-api",
                Base64.getEncoder().encodeToString(
                        ancienne.getPublic().getEncoded()),
                "rsa-ancienne"
        );

        assertNotNull(decoder.decode(
                token(ancienne, "rsa-ancienne", identifiant)));
        assertNotNull(decoder.decode(
                token(nouvelle, "rsa-nouvelle", identifiant)));

        String faux = token(etrangere, "rsa-ancienne", identifiant);
        assertThrows(JwtException.class, () -> decoder.decode(faux));

        JwtDecoder sansAncienne = config.jwtDecoder(
                active, utilisateurs, revocations,
                "superviseme", "superviseme-api", "", ""
        );

        String ancienJwt =
                token(ancienne, "rsa-ancienne", identifiant);
        assertThrows(JwtException.class,
                () -> sansAncienne.decode(ancienJwt));
    }

    @Test
    void refusePairePriveePubliqueIncoherente() throws Exception {
        KeyPair premiere = paire();
        KeyPair deuxieme = paire();

        JwtConfig config = new JwtConfig();

        assertThrows(IllegalStateException.class,
                () -> config.rsaKey(
                        Base64.getEncoder().encodeToString(
                                premiere.getPrivate().getEncoded()),
                        Base64.getEncoder().encodeToString(
                                deuxieme.getPublic().getEncoded()),
                        "rsa-test"));
    }
}
