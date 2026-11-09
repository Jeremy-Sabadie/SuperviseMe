package fr.superviseme.api.configuration.statut;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Accès aux statuts configurés dans la base de l'entreprise.
 *
 * Le tri garantit un affichage cohérent dans les interfaces.
 */
public interface StatutPersonnaliseRepository
        extends JpaRepository<StatutPersonnalise, UUID> {

    /**
     * Liste les statuts d'une catégorie selon l'ordre choisi.
     *
     * @param categorie famille métier recherchée
     * @return statuts triés par position d'affichage
     */
    List<StatutPersonnalise> findByCategorieOrderByOrdreAsc(
            CategorieStatut categorie
    );
}
