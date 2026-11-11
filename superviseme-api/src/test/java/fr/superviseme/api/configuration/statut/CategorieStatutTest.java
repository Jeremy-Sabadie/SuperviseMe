package fr.superviseme.api.configuration.statut;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CategorieStatutTest {

    @Test
    void doitProposerLesTroisCategories() {
        assertArrayEquals(
            new String[]{"CLIENT", "SIGNATURE_COMMERCIALE", "APRES_SIGNATURE"},
            java.util.Arrays.stream(CategorieStatut.values())
                .map(Enum::name)
                .toArray(String[]::new)
        );
    }
}
