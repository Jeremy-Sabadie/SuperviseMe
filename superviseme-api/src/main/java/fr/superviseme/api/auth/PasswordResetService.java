package fr.superviseme.api.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/** Récupération de mot de passe par jeton temporaire à usage unique. */
@Service
public class PasswordResetService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordResetTokenRepository jetons;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mailProvider;
    private final SecureRandom aleatoire = new SecureRandom();

    @Value("${superviseme.password-reset.base-url:http://localhost:4200}")
    private String baseUrl;

    @Value("${superviseme.mail.from:no-reply@localhost}")
    private String expediteur;

    // Activation explicite obligatoire avant tout envoi de lien.
    @Value("${superviseme.password-reset.mail-enabled:false}")
    private boolean mailEnabled;

    public PasswordResetService(
            UtilisateurRepository utilisateurs,
            PasswordResetTokenRepository jetons,
            PasswordEncoder encoder,
            ObjectProvider<JavaMailSender> mailProvider) {
        this.utilisateurs = utilisateurs;
        this.jetons = jetons;
        this.encoder = encoder;
        this.mailProvider = mailProvider;
    }

    /**
     * Réponse publique neutre, même si l'adresse est inconnue.
     * Sans serveur SMTP configuré, aucun jeton inutilisable n'est créé.
     */
    @Transactional
    public void demander(String email) {
        if (email == null || email.isBlank()) return;

        var compte = utilisateurs.findByEmail(
            email.trim().toLowerCase(Locale.ROOT));

        if (compte.isEmpty() || !compte.get().isActif()) return;

        // Aucun lien créé ou envoyé tant que SMTP n'est pas activé.
        if (!mailEnabled) return;

        JavaMailSender mail = mailProvider.getIfAvailable();
        if (mail == null) return;

        byte[] secret = new byte[32];
        aleatoire.nextBytes(secret);
        String token = Base64.getUrlEncoder()
                .withoutPadding().encodeToString(secret);

        jetons.save(new PasswordResetToken(
                compte.get(), empreinte(token),
                Instant.now().plus(15, ChronoUnit.MINUTES)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(compte.get().getEmail());
        message.setSubject("SuperviseMe - Réinitialisation du mot de passe");
        message.setText(
            "Pour définir un nouveau mot de passe, ouvrez ce lien :\n"
            + baseUrl.replaceAll("/+$", "")
            + "/reset-password?token=" + token
            + "\n\nCe lien expire dans 15 minutes et ne peut être utilisé qu'une fois.");

        // Toute erreur SMTP annule la transaction en base.
        mail.send(message);
    }

    /**
     * Verrouille le jeton pour empêcher sa consommation concurrente.
     * Aucun jeton brut ne figure dans la base de données.
     */
    @Transactional
    public void reinitialiser(String token, String motDePasse) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")
                || motDePasse == null
                || motDePasse.length() < 12
                || motDePasse.length() > 128) {
            throw new IllegalArgumentException("Lien ou mot de passe invalide");
        }

        var jeton = jetons.verrouillerParHash(empreinte(token))
                .orElseThrow(() ->
                    new IllegalArgumentException("Lien invalide ou expiré"));

        if (!jeton.estValide(Instant.now())
                || !jeton.getUtilisateur().isActif()) {
            throw new IllegalArgumentException("Lien invalide ou expiré");
        }

        jeton.getUtilisateur().modifierMotDePasseHash(
                encoder.encode(motDePasse));

        // Invalide également les anciens liens du même compte.
        jetons.deleteByUtilisateur_Id(jeton.getUtilisateur().getId());
    }

    private String empreinte(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
