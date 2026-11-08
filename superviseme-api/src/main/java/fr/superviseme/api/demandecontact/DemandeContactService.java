package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.Client;
import fr.superviseme.api.client.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Traite les demandes commerciales reçues depuis le formulaire public.
 *
 * Une demande ne crée jamais automatiquement de dossier.
 */
@Service
public class DemandeContactService {

    private final ClientRepository clientRepository;
    private final DemandeContactRepository demandeRepository;

    public DemandeContactService(
            ClientRepository clientRepository,
            DemandeContactRepository demandeRepository) {
        this.clientRepository = clientRepository;
        this.demandeRepository = demandeRepository;
    }

    /**
     * Retrouve ou crée un client, puis enregistre sa demande.
     * L'ensemble des opérations est transactionnel.
     */
    @Transactional
    public DemandeContact recevoir(
            String nom, String email, String motif, String message) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Nom obligatoire");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire");
        }

        String emailNormalise = email.trim().toLowerCase(Locale.ROOT);

        Client client = clientRepository.findByEmailIgnoreCase(emailNormalise)
                .orElseGet(() -> clientRepository.save(
                        new Client(nom.trim(), null, emailNormalise, null, null)
                ));

        DemandeContact demande = new DemandeContact(client, motif, message);

        return demandeRepository.save(demande);
    }
}
