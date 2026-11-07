package fr.superviseme.api.demandecontact;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Représente une demande commerciale reçue via le formulaire public.
 *
 * Une demande est distincte d'un dossier.
 * La création d'une demande ne déclenche jamais celle d'un dossier.
 */
@Entity
@Table(name = "demandes_contact")
public class DemandeContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String motif;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    /** Constructeur requis par JPA. */
    protected DemandeContact() {
    }

    /**
     * Crée une demande avec un motif et un message obligatoires.
     *
     * @throws IllegalArgumentException si une information est absente ou vide
     */
    public DemandeContact(String motif, String message) {
        this.motif = valider(motif, "Motif");
        this.message = valider(message, "Message");
    }

    /** Vérifie qu'un champ obligatoire est renseigné. */
    private String valider(String valeur, String champ) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(champ + " obligatoire");
        }
        return valeur.trim();
    }

    /** Initialise la date de création avant l'insertion. */
    @PrePersist
    void avantCreation() {
        dateCreation = Instant.now();
    }

    public UUID getId() { return id; }
    public String getMotif() { return motif; }
    public String getMessage() { return message; }
    public Instant getDateCreation() { return dateCreation; }
}
