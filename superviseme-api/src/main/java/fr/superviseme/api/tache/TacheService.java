package fr.superviseme.api.tache;

import fr.superviseme.api.auth.UtilisateurRepository;
import fr.superviseme.api.dossier.DossierService;
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
}
