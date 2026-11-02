package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests des transitions de statut d'une entreprise.
 */
class EntrepriseStatutTest {

    @Test
    void doitActiverUneEntrepriseEnAttente() {
        Entreprise entreprise = nouvelleEntreprise();

        entreprise.activer();

        assertThat(entreprise.getStatut())
                .isEqualTo(StatutEntreprise.ACTIVE);
    }

    @Test
    void doitSignalerUneErreurDeProvisionnement() {
        Entreprise entreprise = nouvelleEntreprise();

        entreprise.signalerErreur();

        assertThat(entreprise.getStatut())
                .isEqualTo(StatutEntreprise.ERREUR);
    }

    @Test
    void doitPermettreUneNouvelleTentativeApresErreur() {
        Entreprise entreprise = nouvelleEntreprise();
        entreprise.signalerErreur();

        entreprise.remettreEnAttente();

        assertThat(entreprise.getStatut())
                .isEqualTo(StatutEntreprise.EN_ATTENTE);
    }

    @Test
    void doitRefuserUneActivationDejaActive() {
        Entreprise entreprise = nouvelleEntreprise();
        entreprise.activer();

        assertThatThrownBy(entreprise::activer)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doitRefuserUneActivationDepuisErreur() {
        Entreprise entreprise = nouvelleEntreprise();
        entreprise.signalerErreur();

        assertThatThrownBy(entreprise::activer)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doitRefuserUneRemiseEnAttenteSansErreur() {
        Entreprise entreprise = nouvelleEntreprise();

        assertThatThrownBy(entreprise::remettreEnAttente)
                .isInstanceOf(IllegalStateException.class);
    }

    private Entreprise nouvelleEntreprise() {
        return new Entreprise("Entreprise Démo", null, null);
    }
}
