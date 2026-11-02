package fr.superviseme.api.entreprise;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service métier responsable de la gestion des entreprises.
 *
 * Centralise les opérations métier et délimite les transactions.
 *
 * Les contrôles d'authentification et d'autorisation devront
 * être intégrés avant d'exposer ces opérations via une API.
 */
@Service
public class EntrepriseService {

    private final EntrepriseRepository repository;

    public EntrepriseService(EntrepriseRepository repository) {
        this.repository = repository;
    }

    /**
     * Crée et enregistre une nouvelle entreprise.
     *
     * La validation du nom est effectuée par l'entité Entreprise.
     * L'opération s'exécute dans une transaction d'écriture.
     *
     * @param nom nom obligatoire
     * @param email adresse électronique facultative
     * @param telephone numéro de téléphone facultatif
     * @return entreprise enregistrée
     * @throws IllegalArgumentException si le nom est invalide
     */
    @Transactional
    public Entreprise creer(String nom, String email, String telephone) {
        Entreprise entreprise = new Entreprise(nom, email, telephone);
        return repository.save(entreprise);
    }

    /**
     * Recherche une entreprise par son identifiant.
     *
     * La transaction est en lecture seule.
     * Cette méthode ne vérifie pas encore les droits du demandeur.
     *
     * @param id identifiant UUID de l'entreprise
     * @return entreprise trouvée ou Optional vide
     */
    @Transactional(readOnly = true)
    public Optional<Entreprise> rechercherParId(UUID id) {
        return repository.findById(id);
    }
}
