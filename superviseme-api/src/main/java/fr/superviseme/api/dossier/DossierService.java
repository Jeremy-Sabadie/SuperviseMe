package fr.superviseme.api.dossier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Gère le cycle de vie des dossiers.
 * Les règles métier restent ici, indépendamment du protocole HTTP.
 */
@Service
@Transactional
public class DossierService {

    private final DossierRepository repository;

    public DossierService(DossierRepository repository) {
        this.repository = repository;
    }

    /** Crée un dossier avec ses valeurs métier par défaut. */
    public Dossier creer(CreerDossierRequest demande) {
        return repository.save(
                new Dossier(demande.titre(), demande.description())
        );
    }

    /** Retourne les dossiers enregistrés. */
    @Transactional(readOnly = true)
    public List<Dossier> lister() {
        return repository.findAll();
    }

    /** Recherche un dossier ou signale son absence. */
    @Transactional(readOnly = true)
    public Dossier consulter(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DossierIntrouvableException(id));
    }

    /** Modifie uniquement les propriétés autorisées par le contrat API. */
    public Dossier modifier(UUID id, ModifierDossierRequest demande) {
        Dossier dossier = consulter(id);

        dossier.modifier(
                demande.titre(),
                demande.description(),
                demande.statut(),
                demande.priorite(),
                demande.echeance()
        );

        return dossier;
    }

    /** Supprime un dossier existant. */
    public void supprimer(UUID id) {
        repository.delete(consulter(id));
    }
}
