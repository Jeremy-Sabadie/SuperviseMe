package fr.superviseme.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

/**
 * Gère les informations personnelles du compte authentifié.
 *
 * Règles de sécurité :
 * - Le compte cible provient exclusivement du JWT validé.
 * - Les rôles et le statut ne sont jamais modifiables ici.
 * - Toute modification sensible exige le mot de passe actuel.
 */
@Service
public class ProfilService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder passwordEncoder;

    public ProfilService(
            UtilisateurRepository utilisateurs,
            PasswordEncoder passwordEncoder) {
        this.utilisateurs = utilisateurs;
        this.passwordEncoder = passwordEncoder;
    }

    /** DTO de lecture : ne jamais exposer le hash du mot de passe. */
    public record Profil(
            UUID id,
            String prenom,
            String nom,
            String email,
            RoleUtilisateur role,
            boolean actif) {}

    @Transactional(readOnly = true)
    public Profil consulter(UUID utilisateurId) {
        return convertir(trouverCompteActif(utilisateurId));
    }

    /**
     * Les deux champs sont facultatifs : une valeur vide indique
     * qu'elle reste « À renseigner » dans l'interface.
     */
    @Transactional
    public Profil modifierIdentite(
            UUID utilisateurId,
            String prenom,
            String nom) {

        Utilisateur compte = trouverCompteActif(utilisateurId);

        compte.modifierIdentite(
                normaliserIdentite(prenom),
                normaliserIdentite(nom));

        return convertir(compte);
    }

    /**
     * Le mot de passe actuel protège la modification d'e-mail.
     * Le contrôleur révoque ensuite le JWT courant.
     */
    @Transactional
    public void modifierEmail(
            UUID utilisateurId,
            String nouvelEmail,
            String motDePasseActuel) {

        Utilisateur compte = trouverCompteActif(utilisateurId);
        verifierMotDePasse(compte, motDePasseActuel);

        String email = normaliserEmail(nouvelEmail);

        if (!email.equals(compte.getEmail())
                && utilisateurs.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Adresse e-mail déjà utilisée");
        }

        compte.modifierEmail(email);
        utilisateurs.saveAndFlush(compte);
    }

    /**
     * Utilise PasswordEncoder : aucun mot de passe en clair
     * n'est conservé dans la base.
     */
    @Transactional
    public void modifierMotDePasse(
            UUID utilisateurId,
            String ancien,
            String nouveau) {

        Utilisateur compte = trouverCompteActif(utilisateurId);
        verifierMotDePasse(compte, ancien);

        if (nouveau == null
                || nouveau.length() < 12
                || nouveau.length() > 128) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le mot de passe doit contenir 12 à 128 caractères");
        }

        if (passwordEncoder.matches(
                nouveau, compte.getMotDePasseHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nouveau mot de passe doit être différent");
        }

        compte.modifierMotDePasseHash(
                passwordEncoder.encode(nouveau));
    }

    private Utilisateur trouverCompteActif(UUID id) {
        return utilisateurs.findById(id)
                .filter(Utilisateur::isActif)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Compte indisponible"));
    }

    private void verifierMotDePasse(
            Utilisateur compte,
            String motDePasse) {

        if (motDePasse == null
                || !passwordEncoder.matches(
                        motDePasse, compte.getMotDePasseHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mot de passe actuel incorrect");
        }
    }

    private String normaliserIdentite(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }

        String resultat = valeur.trim();

        if (resultat.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "100 caractères maximum");
        }

        return resultat;
    }

    private String normaliserEmail(String valeur) {
        if (valeur == null) {
            throw emailInvalide();
        }

        String email = valeur.trim()
                .toLowerCase(Locale.ROOT);

        if (email.length() > 254
                || !email.matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw emailInvalide();
        }

        return email;
    }

    private ResponseStatusException emailInvalide() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Adresse e-mail invalide");
    }

    private Profil convertir(Utilisateur compte) {
        return new Profil(
                compte.getId(),
                compte.getPrenom(),
                compte.getNom(),
                compte.getEmail(),
                compte.getRole(),
                compte.isActif());
    }
}
