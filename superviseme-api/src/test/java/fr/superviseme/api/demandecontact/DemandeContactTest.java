package fr.superviseme.api.demandecontact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires des règles métier d'une demande de contact.
 *
 * Vérifie la conservation des informations et la validation
 * des données indépendamment de la base PostgreSQL.
 */
class DemandeContactTest {

    /**
     * Vérifie qu'une nouvelle demande conserve correctement
     * son motif et son message.
     */
    @Test
    void nouvelleDemandeConserveLeMotifEtLeMessage() {
        // Arrange
        String motif = "Demande de devis";
        String message = "Fabrication d'un escalier sur mesure";

        // Act
        DemandeContact demande = new DemandeContact(motif, message);

        // Assert
        assertEquals(motif, demande.getMotif());
        assertEquals(message, demande.getMessage());
    }

    /**
     * Vérifie qu'une demande sans description est refusée
     * afin d'éviter l'enregistrement de demandes inexploitables.
     */
    @Test
    void nouvelleDemandeRefuseUnMessageVide() {
        // Arrange
        String motif = "Demande de devis";
        String message = " ";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new DemandeContact(motif, message)
        );
    }
}
