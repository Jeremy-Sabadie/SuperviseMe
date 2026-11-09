package fr.superviseme.api.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;

/** Gestion sécurisée des comptes d'une instance cliente. */
@Service
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;

    public UtilisateurService(UtilisateurRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    /**
     * Initialise le premier administrateur d'une instance vierge.
     * Cette méthode ne doit jamais être exposée par une route publique.
     */
    @Transactional
    public Utilisateur initialiserAdministrateur(String email, String motDePasse) {
        if (repository.count() != 0) {
            throw new IllegalStateException("Instance déjà initialisée");
        }
        return creer(email, motDePasse, RoleUtilisateur.ADMIN);
    }

    /** Crée un collaborateur sans privilèges administrateur. */
    @Transactional
    public Utilisateur creerCollaborateur(String email, String motDePasse) {
        return creer(email, motDePasse, RoleUtilisateur.USER);
    }

    @Transactional
    public Utilisateur creerCollaborateur(
            String email, String motDePasse, String prenom, String nom) {
        Utilisateur utilisateur = creerCollaborateur(email, motDePasse);
        utilisateur.modifierIdentite(
                validerIdentite(prenom), validerIdentite(nom));
        return utilisateur;
    }

    @Transactional
    public Utilisateur modifierCollaborateur(
            UUID id, String email, String prenom, String nom) {
        Utilisateur utilisateur = modifierEmail(id, email);
        utilisateur.modifierIdentite(
                validerIdentite(prenom), validerIdentite(nom));
        return utilisateur;
    }

    private String validerIdentite(String valeur) {
        if (valeur == null || valeur.isBlank() || valeur.trim().length() > 100) {
            throw new IllegalArgumentException("Prénom et nom obligatoires (100 caractères maximum)");
        }
        return valeur.trim();
    }

    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> lister() {
        return repository.findAll();
    }

    /**
     * Désactive un collaborateur sans supprimer ses données historiques.
     * L'administrateur ne peut pas désactiver son propre compte.
     */
    @Transactional
    public Utilisateur desactiver(UUID cibleId, UUID administrateurId) {
        if (cibleId.equals(administrateurId)) {
            throw new AccessDeniedException("Auto-désactivation interdite");
        }

        Utilisateur cible = repository.findById(cibleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Collaborateur introuvable"));

        cible.desactiver();
        return cible;
    }

    /** Réactive un compte existant en conservant son historique. */
    @Transactional
    public Utilisateur reactiver(UUID cibleId) {
        Utilisateur cible = repository.findById(cibleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Collaborateur introuvable"));

        cible.reactiver();
        return cible;
    }

    @Transactional
    public Utilisateur modifierEmail(UUID id, String email) {
        Utilisateur cible = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collaborateur introuvable"));

        if (cible.getRole() == RoleUtilisateur.ADMIN) {
            throw new IllegalArgumentException("Modification administrateur interdite");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire");
        }

        String normalise = email.trim().toLowerCase(Locale.ROOT);
        if (!normalise.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || normalise.length() > 254) {
            throw new IllegalArgumentException("Email invalide");
        }

        if (!normalise.equals(cible.getEmail()) && repository.existsByEmail(normalise)) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        cible.modifierEmail(normalise);
        return cible;
    }

    private Utilisateur creer(String email, String motDePasse, RoleUtilisateur role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire");
        }

        String emailNormalise = email.trim().toLowerCase(Locale.ROOT);

        if (!emailNormalise.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || emailNormalise.length() > 254) {
            throw new IllegalArgumentException("Email invalide");
        }

        if (motDePasse == null || motDePasse.length() < 12) {
            throw new IllegalArgumentException("Mot de passe trop court");
        }

        if (repository.existsByEmail(emailNormalise)) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        return repository.save(new Utilisateur(
                emailNormalise,
                encoder.encode(motDePasse),
                role
        ));
    }
}
