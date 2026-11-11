package fr.superviseme.api.agenda;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Règles métier de visibilité des rendez-vous.
 * ADMIN gère tous les rendez-vous.
 * USER consulte uniquement les rendez-vous partagés.
 */
class VisibiliteRendezVousTest {

    @Test
    void unRendezVousEstPriveParDefaut() {
        RendezVous rendezVous = new RendezVous();

        assertFalse(rendezVous.isPartage());
    }

    @Test
    void unAdministrateurPeutConsulterUnRendezVousPrive() {
        RendezVous rendezVous = new RendezVous();

        assertTrue(rendezVous.estVisiblePar("ADMIN"));
    }

    @Test
    void unCollaborateurNePeutPasConsulterUnRendezVousPrive() {
        RendezVous rendezVous = new RendezVous();

        assertFalse(rendezVous.estVisiblePar("USER"));
    }

    @Test
    void unCollaborateurPeutConsulterUnRendezVousPartage() {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setPartage(true);

        assertTrue(rendezVous.estVisiblePar("USER"));
    }

    @Test
    void unRoleInconnuNePeutPasConsulterUnRendezVous() {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setPartage(true);

        assertFalse(rendezVous.estVisiblePar("UNKNOWN"));
    }
}
