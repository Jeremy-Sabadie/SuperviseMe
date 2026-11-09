package fr.superviseme.api.client;

import fr.superviseme.api.dossier.DossierRepository;
import fr.superviseme.api.configuration.statut.CategorieStatut;
import fr.superviseme.api.configuration.statut.StatutPersonnaliseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Centralise les opérations métier sur les clients.
 */
@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository repository;
    private final DossierRepository dossierRepository;
    private final StatutPersonnaliseRepository statutsRepository;

    public ClientService(
            ClientRepository repository,
            DossierRepository dossierRepository,
            StatutPersonnaliseRepository statutsRepository) {
        this.repository = repository;
        this.dossierRepository = dossierRepository;
        this.statutsRepository = statutsRepository;
    }

    /** Crée un client dans le référentiel de l'instance. */
    @Transactional
    public ClientResponse creer(ClientRequest request) {
        Client client = new Client(
                request.nom(), request.entreprise(), request.email(),
                request.telephone(), request.adresse()
        );

        return ClientResponse.depuis(repository.saveAndFlush(client));
    }

    /** Liste ou recherche les clients par nom. */
    public List<ClientResponse> lister(String recherche) {
        List<Client> clients = recherche == null || recherche.isBlank()
                ? repository.findAllByOrderByNomAsc()
                : repository.findByNomContainingIgnoreCaseOrderByNomAsc(
                        recherche.trim()
                );

        return clients.stream().map(ClientResponse::depuis).toList();
    }

    /** Consulte un client existant. */
    public ClientResponse consulter(UUID id) {
        return ClientResponse.depuis(trouver(id));
    }

    /** Modifie les coordonnées d'un client. */
    @Transactional
    public ClientResponse modifier(UUID id, ClientRequest request) {
        Client client = trouver(id);

        client.modifier(
                request.nom(), request.entreprise(), request.email(),
                request.telephone(), request.adresse()
        );

        return ClientResponse.depuis(repository.saveAndFlush(client));
    }

    /**
     * Supprime un client sans supprimer ses dossiers.
     * La base interdit la suppression d'un client encore référencé.
     */
    @Transactional
    public void supprimer(UUID id) {
        Client client = trouver(id);

        if (dossierRepository.existsByClientId(id)) {
            throw new ClientAvecDossiersException(id);
        }

        repository.delete(client);
        repository.flush();
    }

    /** Affecte uniquement un statut de catégorie CLIENT. */
    @Transactional
    public ClientResponse changerStatutPersonnalise(UUID id, UUID statutId) {
        Client client = trouver(id);

        var statut = statutsRepository.findById(statutId)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable"));

        if (statut.getCategorie() != CategorieStatut.CLIENT) {
            throw new IllegalArgumentException("Statut non autorisé pour un client");
        }

        client.changerStatutPersonnalise(statutId);
        return ClientResponse.depuis(repository.saveAndFlush(client));
    }

    /** Recherche l'entité ou signale son absence. */
    private Client trouver(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ClientIntrouvableException(id));
    }
}
