package fr.superviseme.api.configuration.statut;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Vérifie le déplacement des statuts sans doublon de position. */
@ExtendWith(MockitoExtension.class)
class StatutReordonnancementTest {
    @Mock StatutPersonnaliseRepository repository;
    @InjectMocks StatutPersonnaliseService service;

    @Test
    void deplacerPermuteLesPositionsDesStatuts() {
        StatutPersonnalise premier = new StatutPersonnalise(
                CategorieStatut.CLIENT, "Prospect", "#112233", 0);
        StatutPersonnalise second = new StatutPersonnalise(
                CategorieStatut.CLIENT, "Contacté", "#445566", 1);

        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(premier));
        when(repository.findByCategorieOrderByOrdreAsc(CategorieStatut.CLIENT))
                .thenReturn(List.of(premier, second));

        service.reordonner(id, 1);

        assertEquals(1, premier.getOrdre());
        assertEquals(0, second.getOrdre());
    }
}
