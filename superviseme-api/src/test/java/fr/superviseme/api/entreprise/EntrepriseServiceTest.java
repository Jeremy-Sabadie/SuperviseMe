package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie les règles métier de gestion des entreprises.
 *
 * Chaque test est exécuté dans une transaction annulée
 * afin de ne pas conserver ses données en base.
 */
@SpringBootTest
@Transactional
class EntrepriseServiceTest {

    @Autowired
    private EntrepriseService service;

    @Test
    void doitCreerEtRetrouverUneEntreprise() {
        Entreprise entreprise = service.creer(
                "Entreprise Démo",
                "contact@example.com",
                "0559000000"
        );

        Entreprise resultat = service.rechercherParId(entreprise.getId())
                .orElseThrow();

        assertThat(resultat.getNom()).isEqualTo("Entreprise Démo");
        assertThat(resultat.getId()).isNotNull();
    }

    @Test
    void doitRefuserUnNomVide() {
        assertThatThrownBy(() -> service.creer(" ", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Une entreprise nouvellement enregistrée ne doit pas
     * être considérée comme active avant son provisionnement.
     */
    @Test
    void doitCreerUneEntrepriseEnAttente() {
        Entreprise entreprise = service.creer(
                "Menuiserie Dupont",
                "contact@exemple.fr",
                null
        );

        assertThat(entreprise.getStatut())
                .isEqualTo(StatutEntreprise.EN_ATTENTE);

        Entreprise resultat = service.rechercherParId(entreprise.getId())
                .orElseThrow();

        assertThat(resultat.getStatut())
                .isEqualTo(StatutEntreprise.EN_ATTENTE);
    }
}
