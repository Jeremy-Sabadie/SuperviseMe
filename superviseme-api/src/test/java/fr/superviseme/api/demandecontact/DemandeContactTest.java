package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.Client;
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
        Client client = new Client("Dupont", null, "dupont@example.com", null, null);
        DemandeContact demande = new DemandeContact(client, motif, message);

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
        Client client = new Client("Dupont", null, "dupont@example.com", null, null);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new DemandeContact(client, motif, message)
        );
    }
}
