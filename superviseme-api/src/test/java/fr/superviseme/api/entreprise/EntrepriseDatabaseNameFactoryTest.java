package fr.superviseme.api.entreprise;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

 /**
  * Vérifie la génération des noms de bases PostgreSQL dédiées.
  *
  * Les noms doivent être déterministes, uniques par entreprise
  * et compatibles avec les identifiants PostgreSQL.
  */
class EntrepriseDatabaseNameFactoryTest {

    private final EntrepriseDatabaseNameFactory factory =
            new EntrepriseDatabaseNameFactory();

    @Test
    void doitGenererUnNomStablePourUneEntreprise() {
        UUID entrepriseId = UUID.fromString(
                "123e4567-e89b-12d3-a456-426614174000"
        );

        String premierNom = factory.generer(entrepriseId);
        String secondNom = factory.generer(entrepriseId);

        assertThat(premierNom).isEqualTo(secondNom);
        assertThat(premierNom)
                .matches("superviseme_[a-f0-9]{32}");
    }

    @Test
    void doitGenererDesNomsDistinctsPourDeuxEntreprises() {
        UUID premiereEntreprise = UUID.randomUUID();
        UUID secondeEntreprise = UUID.randomUUID();

        assertThat(factory.generer(premiereEntreprise))
                .isNotEqualTo(factory.generer(secondeEntreprise));
    }

    @Test
    void doitRefuserUnIdentifiantAbsent() {
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> factory.generer(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
