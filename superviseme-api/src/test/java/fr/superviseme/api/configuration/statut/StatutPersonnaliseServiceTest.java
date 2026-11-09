package fr.superviseme.api.configuration.statut;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

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

    /**
     * Vérifie qu'un nouveau statut est enregistré dans
     * la base de données de l'entreprise cliente.
     */
    @Test
    void doitCreerUnStatutPersonnalise() {
        StatutPersonnalise statut = new StatutPersonnalise(
                CategorieStatut.CLIENT, "Prospect", "#2563EB", 1
        );

        when(repository.save(statut)).thenReturn(statut);

        StatutPersonnalise resultat = service.creer(statut);

        assertSame(statut, resultat);
        verify(repository).save(statut);
    }

    /**
     * Vérifie qu'un statut existant peut être personnalisé
     * sans changer son identifiant ni sa catégorie.
     */
    @Test
    void doitModifierUnStatutPersonnalise() {
        UUID id = UUID.randomUUID();

        StatutPersonnalise statut = new StatutPersonnalise(
                CategorieStatut.CLIENT, "Prospect", "#2563EB", 1
        );

        when(repository.findById(id)).thenReturn(Optional.of(statut));

        service.modifier(id, "Client potentiel", "#16A34A", 2);

        assertAll(
                () -> assertEquals("Client potentiel", statut.getLibelle()),
                () -> assertEquals("#16A34A", statut.getCouleur()),
                () -> assertEquals(2, statut.getOrdre()),
                () -> assertEquals(CategorieStatut.CLIENT, statut.getCategorie())
        );
    }

}
