package fr.superviseme.api.configuration.statut;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service métier de gestion des statuts personnalisables.
 *
 * Chaque entreprise dispose de sa propre base de données.
 * Les statuts existants ne sont ni modifiés ni remplacés.
 */
@Service
public class StatutPersonnaliseService {

    private final StatutPersonnaliseRepository repository;

    /**
     * Injection explicite de la dépendance de persistance.
     *
     * @param repository accès aux statuts personnalisés
     */
    public StatutPersonnaliseService(
            StatutPersonnaliseRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Retourne les statuts d'une catégorie dans l'ordre configuré.
     *
     * @param categorie catégorie métier recherchée
     * @return liste ordonnée des statuts personnalisés
     * @throws NullPointerException si la catégorie est absente
     */
    @Transactional(readOnly = true)
    public List<StatutPersonnalise> lister(CategorieStatut categorie) {
        Objects.requireNonNull(categorie, "La catégorie est obligatoire");

        return repository.findByCategorieOrderByOrdreAsc(categorie);
    }
}
