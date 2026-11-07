package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.Client;
import fr.superviseme.api.client.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du traitement des demandes du formulaire public.
 *
 * Une demande crée ou retrouve un client, sans créer de dossier.
 */
@ExtendWith(MockitoExtension.class)
class DemandeContactServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DemandeContactRepository demandeRepository;

    private DemandeContactService service;

    @BeforeEach
    void preparer() {
        service = new DemandeContactService(
                clientRepository, demandeRepository
        );
    }

    @Test
    void reutiliseUnClientExistant() {
        // Arrange
        Client client = new Client(
                "Dupont", null, "dupont@example.com", null, null
        );
        when(clientRepository.findByEmailIgnoreCase("dupont@example.com"))
                .thenReturn(Optional.of(client));
        when(demandeRepository.save(any(DemandeContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DemandeContact demande = service.recevoir(
                "Dupont", "dupont@example.com",
                "Devis", "Fabrication d'un escalier"
        );

        // Assert
        assertSame(client, demande.getClient());
        verify(clientRepository, never()).save(any(Client.class));
        verify(demandeRepository).save(any(DemandeContact.class));
    }

    @Test
    void creeUnClientInexistant() {
        // Arrange
        when(clientRepository.findByEmailIgnoreCase("nouveau@example.com"))
                .thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(demandeRepository.save(any(DemandeContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DemandeContact demande = service.recevoir(
                "Martin", "nouveau@example.com",
                "Contact", "Je souhaite un rendez-vous"
        );

        // Assert
        assertEquals("Martin", demande.getClient().getNom());
        assertEquals("nouveau@example.com", demande.getClient().getEmail());
        verify(clientRepository).save(any(Client.class));
        verify(demandeRepository).save(any(DemandeContact.class));
    }

    @Test
    void conserveLaDemandeSansCreerDeDossier() {
        // Arrange
        Client client = new Client(
                "Dupont", null, "dupont@example.com", null, null
        );
        when(clientRepository.findByEmailIgnoreCase("dupont@example.com"))
                .thenReturn(Optional.of(client));
        when(demandeRepository.save(any(DemandeContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DemandeContact demande = service.recevoir(
                "Dupont", "dupont@example.com",
                "Devis", "Création d'une terrasse"
        );

        // Assert
        assertEquals("Devis", demande.getMotif());
        assertEquals("Création d'une terrasse", demande.getMessage());
        verify(demandeRepository).save(demande);
    }
}
