package fr.superviseme.api.tache;

import fr.superviseme.api.auth.UtilisateurRepository;
import fr.superviseme.api.dossier.DossierService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
