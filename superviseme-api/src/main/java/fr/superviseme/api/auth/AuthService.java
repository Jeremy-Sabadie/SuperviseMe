package fr.superviseme.api.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Vérification des identifiants et émission des JWT. */
@Service
public class AuthService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;

    @Value("${superviseme.jwt.active-key-id:superviseme-1}")
    private String jwtKeyId = "superviseme-1";

    private String jwtIssuer = "superviseme";
    private String jwtAudience = "superviseme-api";

    @Value("${superviseme.jwt.issuer:superviseme}")
    void configurerIssuer(String issuer) {
        this.jwtIssuer = issuer;
    }

    @Value("${superviseme.jwt.audience:superviseme-api}")
    void configurerAudience(String audience) {
        this.jwtAudience = audience;
    }

    public AuthService(
            UtilisateurRepository repository,
            PasswordEncoder encoder,
            JwtEncoder jwtEncoder
    ) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
    }

    public String connecter(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        Utilisateur utilisateur = repository
                .findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                .orElseThrow(() ->
                        new BadCredentialsException("Identifiants incorrects"));

        // Ne révèle pas si le compte existe ou s'il est désactivé.
        if (!utilisateur.isActif()
                || !encoder.matches(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        Instant maintenant = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .audience(java.util.List.of(jwtAudience))
                .subject(utilisateur.getId().toString())
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(30, ChronoUnit.MINUTES))
                .claim("roles", java.util.List.of(utilisateur.getRole().name()))
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(jwtKeyId).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
