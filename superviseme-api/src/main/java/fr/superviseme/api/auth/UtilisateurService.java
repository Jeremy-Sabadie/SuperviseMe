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
