package fr.superviseme.api.configuration.statut;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
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

    /**
     * Enregistre un nouveau statut personnalisé.
     *
     * @param statut statut préalablement validé par son constructeur
     * @return statut enregistré
     */
    @Transactional
    public StatutPersonnalise creer(StatutPersonnalise statut) {
        Objects.requireNonNull(statut, "Le statut est obligatoire");
        return repository.save(statut);
    }

    /**
     * Modifie un statut existant sans changer son identité.
     *
     * La transaction permet à JPA de persister les changements
     * sur l'entité récupérée.
     *
     * @throws IllegalArgumentException si le statut est introuvable
     *                                  ou si les données sont invalides
     */
    @Transactional
    public StatutPersonnalise modifier(
            UUID id, String libelle, String couleur, int ordre
    ) {
        Objects.requireNonNull(id, "L'identifiant est obligatoire");

        StatutPersonnalise statut = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Statut introuvable")
                );

        statut.modifier(libelle, couleur, ordre);
        return statut;
    }


    /**
     * Supprime un statut existant.
     *
     * ATTENTION : la vérification des références métier
     * doit être ajoutée avant toute exposition de cette
     * opération dans l'API publique.
     *
     * @param id identifiant du statut à supprimer
     * @throws IllegalArgumentException si le statut est introuvable
     */
    @Transactional
    public void supprimer(UUID id) {
        Objects.requireNonNull(id, "L'identifiant est obligatoire");

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Statut introuvable");
        }

        repository.deleteById(id);
    }

    /**
     * Modifie la position d'affichage d'un statut.
     *
     * Cette opération conserve le libellé, la couleur
     * et la catégorie du statut.
     *
     * @param id identifiant du statut
     * @param ordre nouvelle position
     * @throws IllegalArgumentException si le statut ou l'ordre est invalide
     */
    @Transactional
    public void reordonner(UUID id, int ordre) {
        Objects.requireNonNull(id, "L'identifiant est obligatoire");

        StatutPersonnalise statut = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Statut introuvable")
                );

        statut.modifier(
                statut.getLibelle(),
                statut.getCouleur(),
                ordre
        );
    }

}
