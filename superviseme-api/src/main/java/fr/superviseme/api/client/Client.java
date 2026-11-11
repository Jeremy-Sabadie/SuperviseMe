package fr.superviseme.api.client;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Représente un client enregistré dans une instance SuperviseMe.
 * Chaque instance possède son propre référentiel clients.
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 150)
    private String entreprise;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(nullable = false)
    private Instant dateModification;

    @Column(name = "statut_personnalise_id")
    private UUID statutPersonnaliseId;

    public UUID getStatutPersonnaliseId() {
        return statutPersonnaliseId;
    }

    public void changerStatutPersonnalise(UUID statutId) {
        this.statutPersonnaliseId = statutId;
    }

    /** Constructeur requis par JPA. */
    protected Client() {
    }

    /**
     * Crée un client avec ses coordonnées.
     *
     * @throws IllegalArgumentException si le nom est invalide
     */
    public Client(String nom, String entreprise, String email,
                  String telephone, String adresse) {
        modifier(nom, entreprise, email, telephone, adresse);
    }

    /**
     * Met à jour les informations du client.
     */
    public void modifier(String nom, String entreprise, String email,
                         String telephone, String adresse) {
        if (nom == null || nom.isBlank() || nom.trim().length() > 150) {
            throw new IllegalArgumentException("Nom du client invalide");
        }

        this.nom = nom.trim();
        this.entreprise = entreprise;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    /** Initialise les dates à la création. */
    @PrePersist
    void avantCreation() {
        dateCreation = Instant.now();
        dateModification = dateCreation;
    }

    /** Actualise la date de modification. */
    @PreUpdate
    void avantModification() {
        dateModification = Instant.now();
    }

    public UUID getId() { return id; }
    public String getNom() { return nom; }
    public String getEntreprise() { return entreprise; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getAdresse() { return adresse; }
    public Instant getDateCreation() { return dateCreation; }
    public Instant getDateModification() { return dateModification; }
}
