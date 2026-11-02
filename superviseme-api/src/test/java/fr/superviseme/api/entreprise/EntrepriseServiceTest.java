package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
