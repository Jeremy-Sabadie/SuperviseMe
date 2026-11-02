package fr.superviseme.api.entreprise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Représente une entreprise cliente de SuperviseMe.
 *
 * Chaque entreprise possède un identifiant unique qui permettra
 * d'isoler ses données de celles des autres entreprises.
 *
 * La gestion des droits d'accès sera assurée séparément.
 */
@Entity
@Table(name = "entreprises")
public class Entreprise {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Constructeur sans argument requis par JPA.
     */
    protected Entreprise() {
    }

    /**
     * Crée une entreprise avec un identifiant UUID.
     *
     * @param nom nom obligatoire de l'entreprise
     * @param email adresse électronique facultative
     * @param telephone numéro de téléphone facultatif
     * @throws IllegalArgumentException si le nom est absent ou vide
     */
    public Entreprise(String nom, String email, String telephone) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de l'entreprise est obligatoire"
            );
        }

        this.id = UUID.randomUUID();
        this.nom = nom.trim();
        this.email = email;
        this.telephone = telephone;
    }

    /**
     * Initialise les horodatages avant la première insertion.
     */
    @PrePersist
    protected void avantCreation() {
        Instant maintenant = Instant.now();
        createdAt = maintenant;
        updatedAt = maintenant;
    }

    /**
     * Actualise la date de modification avant une mise à jour.
     */
    @PreUpdate
    protected void avantModification() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
