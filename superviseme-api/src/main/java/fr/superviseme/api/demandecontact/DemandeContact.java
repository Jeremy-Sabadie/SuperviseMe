package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.Client;
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

    /** Client à l'origine de la demande commerciale. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 150)
    private String motif;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    /**
     * Étapes du suivi commercial d'une demande.
     * Les anciens statuts restent disponibles pour les données existantes.
     */
    public enum Statut {
        A_TRAITER,
        EN_COURS,
        TRAITE,
        PROPOSITION_COMMERCIALE_A_ETABLIR,
        PROPOSITION_COMMERCIALE_ENVOYEE,
        PROPOSITION_COMMERCIALE_ACCEPTEE,
        PROPOSITION_COMMERCIALE_REFUSEE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Statut statut = Statut.A_TRAITER;

    /** Constructeur requis par JPA. */
    protected DemandeContact() {
    }

    /**
     * Crée une demande commerciale associée à un client.
     *
     * @throws IllegalArgumentException si le client, le motif
     *                                  ou le message est invalide
     */
    public DemandeContact(Client client, String motif, String message) {
        if (client == null) {
            throw new IllegalArgumentException("Client obligatoire");
        }

        this.client = client;
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

    public Statut getStatut() { return statut; }

    public void changerStatut(Statut nouveauStatut) {
        if (nouveauStatut == null) {
            throw new IllegalArgumentException("Statut obligatoire");
        }
        this.statut = nouveauStatut;
    }

    public UUID getId() { return id; }
    public Client getClient() { return client; }
    public String getMotif() { return motif; }
    public String getMessage() { return message; }
    public Instant getDateCreation() { return dateCreation; }
}
