package fr.superviseme.api.entreprise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Représente une entreprise cliente dans le registre central SuperviseMe.
 *
 * Chaque entreprise possède un identifiant unique.
 * Ses données métier seront hébergées dans une instance dédiée.
 *
 * La gestion des autorisations et le provisionnement sont
 * assurés par des composants distincts.
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEntreprise statut;

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
     * Crée une entreprise dans l'état EN_ATTENTE.
     *
     * L'enregistrement ne déclenche aucun déploiement.
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
        this.statut = StatutEntreprise.EN_ATTENTE;
    }

    /**
     * Initialise les horodatages avant l'insertion.
     */
    @PrePersist
    protected void avantCreation() {
        Instant maintenant = Instant.now();
        createdAt = maintenant;
        updatedAt = maintenant;
    }

    /**
     * Actualise la date de dernière modification.
     */
    @PreUpdate
    protected void avantModification() {
        updatedAt = Instant.now();
    }

    /**
     * Marque l'entreprise active après provisionnement réussi.
     *
     * Seul le service de provisionnement devra appeler cette méthode.
     */
    public void activer() {
        verifierStatut(StatutEntreprise.EN_ATTENTE);
        statut = StatutEntreprise.ACTIVE;
    }

    /**
     * Signale un échec pendant le provisionnement.
     */
    public void signalerErreur() {
        verifierStatut(StatutEntreprise.EN_ATTENTE);
        statut = StatutEntreprise.ERREUR;
    }

    /**
     * Autorise une nouvelle tentative après un échec.
     */
    public void remettreEnAttente() {
        verifierStatut(StatutEntreprise.ERREUR);
        statut = StatutEntreprise.EN_ATTENTE;
    }

    /**
     * Empêche une transition depuis un état inattendu.
     *
     * @param attendu état requis avant la transition
     */
    private void verifierStatut(StatutEntreprise attendu) {
        if (statut != attendu) {
            throw new IllegalStateException(
                    "Transition impossible depuis le statut " + statut
            );
        }
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

    public StatutEntreprise getStatut() {
        return statut;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
