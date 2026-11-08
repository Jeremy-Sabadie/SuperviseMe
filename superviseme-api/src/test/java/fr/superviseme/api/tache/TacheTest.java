package fr.superviseme.api.tache;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TacheTest {

    @Test
    void creerTacheIndependante() {
        Tache tache = new Tache("Entretenir les machines", null);

        assertEquals("Entretenir les machines", tache.getTitre());
        assertNull(tache.getDossierId());
        assertEquals(StatutTache.NON_COMMENCEE, tache.getStatut());
        assertEquals(PrioriteTache.NORMALE, tache.getPriorite());
    }

    @Test
    void creerTacheLieeADossier() {
        UUID dossierId = UUID.randomUUID();

        Tache tache = new Tache("Commander le bois", dossierId);

        assertEquals(dossierId, tache.getDossierId());
    }

    @Test
    void refuserTitreVide() {
        assertThrows(IllegalArgumentException.class,
                () -> new Tache("   ", null));
    }

    @Test
    void changerStatut() {
        Tache tache = new Tache("Préparer le chantier", null);

        tache.changerStatut(StatutTache.EN_COURS);

        assertEquals(StatutTache.EN_COURS, tache.getStatut());
    }
}
