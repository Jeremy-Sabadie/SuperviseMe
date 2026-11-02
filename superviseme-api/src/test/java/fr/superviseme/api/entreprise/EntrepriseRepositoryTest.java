package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EntrepriseRepositoryTest {

    @Autowired
    private EntrepriseRepository repository;

    @Test
    void doitEnregistrerEtRetrouverUneEntreprise() {
        Entreprise entreprise = new Entreprise(
                "Atelier Test",
                "contact@example.com",
                "0559000000"
        );

        repository.saveAndFlush(entreprise);

        Entreprise resultat = repository.findById(entreprise.getId())
                .orElseThrow();

        assertThat(resultat.getNom()).isEqualTo("Atelier Test");
        assertThat(resultat.getEmail()).isEqualTo("contact@example.com");
        assertThat(resultat.getTelephone()).isEqualTo("0559000000");
        assertThat(resultat.getCreatedAt()).isNotNull();
        assertThat(resultat.getUpdatedAt()).isNotNull();
    }
}
