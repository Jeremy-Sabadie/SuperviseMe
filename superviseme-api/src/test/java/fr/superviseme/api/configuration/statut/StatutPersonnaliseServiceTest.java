package fr.superviseme.api.configuration.statut;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Vérifie les règles métier de consultation des statuts.
 *
 * Chaque catégorie possède sa propre liste de statuts.
 * Le repository assure le classement par ordre d'affichage.
 */
@ExtendWith(MockitoExtension.class)
class StatutPersonnaliseServiceTest {

    @Mock
    private StatutPersonnaliseRepository repository;

    @InjectMocks
    private StatutPersonnaliseService service;

    /**
     * Vérifie que seuls les statuts de la catégorie demandée
     * sont récupérés, dans l'ordre défini par le propriétaire.
     */
    @Test
    void doitListerLesStatutsParCategorie() {
        CategorieStatut categorie = CategorieStatut.SIGNATURE_COMMERCIALE;

        StatutPersonnalise statut = new StatutPersonnalise(
                categorie, "Devis envoyé", "#3B82F6", 1
        );

        when(repository.findByCategorieOrderByOrdreAsc(categorie))
                .thenReturn(List.of(statut));

        List<StatutPersonnalise> resultat = service.lister(categorie);

        assertEquals(1, resultat.size());
        assertEquals("Devis envoyé", resultat.getFirst().getLibelle());

        verify(repository).findByCategorieOrderByOrdreAsc(categorie);
    }
}
