package fr.superviseme.api.configuration.statut;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du modèle de statut personnalisable.
 *
 * Vérifie les règles métier communes aux statuts configurés
 * par le propriétaire d'une entreprise cliente.
 */
class StatutPersonnaliseTest {

    /**
     * Vérifie qu'un statut conserve correctement sa catégorie,
     * son libellé, sa couleur et sa position d'affichage.
     */
    @Test
    void doitCreerUnStatutPersonnalise() {
        StatutPersonnalise statut = new StatutPersonnalise(
                CategorieStatut.SIGNATURE_COMMERCIALE,
                "Devis envoyé",
                "#3B82F6",
                2
        );

        assertAll(
                () -> assertEquals("Devis envoyé", statut.getLibelle()),
                () -> assertEquals("#3B82F6", statut.getCouleur()),
                () -> assertEquals(2, statut.getOrdre()),
                () -> assertEquals(
                        CategorieStatut.SIGNATURE_COMMERCIALE,
                        statut.getCategorie()
                )
        );
    }

    /**
     * Empêche la création d'un statut sans libellé exploitable.
     *
     * Cette validation protège la cohérence des listes
     * et des tableaux de suivi affichés aux collaborateurs.
     */
    @Test
    void doitRefuserUnLibelleVide() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatutPersonnalise(
                        CategorieStatut.CLIENT,
                        " ",
                        "#3B82F6",
                        1
                )
        );
    }
}
