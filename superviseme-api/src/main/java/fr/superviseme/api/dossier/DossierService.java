package fr.superviseme.api.dossier;

import fr.superviseme.api.client.ClientRepository;
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
    private final ClientRepository clientRepository;

    public DossierService(
            DossierRepository repository,
            ClientRepository clientRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
    }

    /** Crée un dossier avec ses valeurs métier par défaut. */
    public Dossier creer(CreerDossierRequest demande) {
        verifierClientExistant(demande.clientId());

        Dossier dossier = new Dossier(
                demande.titre(),
                demande.description()
        );
        dossier.associerClient(demande.clientId());

        return repository.save(dossier);
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
        verifierClientExistant(demande.clientId());

        dossier.modifier(
                demande.titre(),
                demande.description(),
                demande.statut(),
                demande.priorite(),
                demande.echeance()
        );
        dossier.associerClient(demande.clientId());

        return dossier;
    }

    /**
     * Vérifie l'existence d'un client avant de l'associer à un dossier.
     * Une association absente est autorisée.
     */
    private void verifierClientExistant(UUID clientId) {
        if (clientId != null && !clientRepository.existsById(clientId)) {
            throw new ClientDossierIntrouvableException(clientId);
        }
    }

    /** Supprime un dossier existant. */
    public void supprimer(UUID id) {
        repository.delete(consulter(id));
    }
}
