package fr.superviseme.api.tache;

import fr.superviseme.api.auth.UtilisateurRepository;
import fr.superviseme.api.dossier.DossierService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Centralise les règles métier de gestion des tâches.
 */
@Service
@Transactional
public class TacheService {

    private final TacheRepository repository;
    private final UtilisateurRepository utilisateurs;
    private final DossierService dossiers;

    public TacheService(
            TacheRepository repository,
            UtilisateurRepository utilisateurs,
            DossierService dossiers) {
        this.repository = repository;
        this.utilisateurs = utilisateurs;
        this.dossiers = dossiers;
    }

    /**
     * Crée une tâche indépendante ou liée à un dossier accessible.
     */
    public Tache creer(CreerTacheRequest demande) {
        verifierAdministrateur();

        UUID responsableId = demande.responsableId();

        if (responsableId == null) {
            throw new IllegalArgumentException("Responsable obligatoire");
        }

        if (demande.dossierId() != null) {
            dossiers.consulter(demande.dossierId());
        }

        var responsable = utilisateurs.findById(responsableId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Responsable introuvable"));

        if (!responsable.isActif()) {
            throw new IllegalArgumentException("Responsable désactivé");
        }

        Tache tache = new Tache(demande.titre(), demande.dossierId());
        tache.affecterResponsable(responsableId);

        return repository.save(tache);
    }

    /**
     * Liste toutes les tâches de l'instance pour les utilisateurs autorisés.
     */
    @Transactional(readOnly = true)
    public List<Tache> lister() {
        verifierUtilisateurAutorise();
        return repository.findAll();
    }

    /**
     * ADMIN peut changer tous les statuts.
     * USER peut changer uniquement le statut de ses tâches.
     */
    public Tache changerStatut(UUID id, StatutTache statut) {
        verifierUtilisateurAutorise();

        Tache tache = repository.findById(id)
                .orElseThrow(() -> new TacheIntrouvableException(id));

        if (!estAdministrateur()) {
            UUID utilisateurId = utilisateurConnecteId();

            if (!java.util.Objects.equals(
                    utilisateurId, tache.getResponsableId())) {
                throw new AccessDeniedException(
                        "Modification réservée au responsable de la tâche");
            }
        }

        tache.changerStatut(statut);
        return tache;
    }

    private void verifierUtilisateurAutorise() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(role ->
                        "ROLE_ADMIN".equals(role.getAuthority())
                        || "ROLE_USER".equals(role.getAuthority()))) {
            throw new AccessDeniedException("Accès aux tâches interdit");
        }
    }

    private boolean estAdministrateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()));
    }

    private UUID utilisateurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void verifierAdministrateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream()
                        .noneMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))) {
            throw new AccessDeniedException(
                    "Création des tâches réservée aux administrateurs");
        }
    }
}
