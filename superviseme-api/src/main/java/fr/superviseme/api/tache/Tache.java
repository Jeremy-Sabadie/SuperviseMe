package fr.superviseme.api.tache;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Action à réaliser, liée ou non à un dossier.
 */
@Entity
@Table(name = "taches")
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "dossier_id")
    private UUID dossierId;

    @Column(name = "responsable_id")
    private UUID responsableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutTache statut = StatutTache.NON_COMMENCEE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioriteTache priorite = PrioriteTache.NORMALE;

    private LocalDate echeance;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(nullable = false)
    private Instant dateModification;

    protected Tache() {
    }

    public Tache(String titre, UUID dossierId) {
        definirTitre(titre);
        this.dossierId = dossierId;
    }

    private void definirTitre(String titre) {
        if (titre == null || titre.isBlank()
                || titre.trim().length() > 200) {
            throw new IllegalArgumentException("Titre de tâche invalide");
        }
        this.titre = titre.trim();
    }

    public void changerStatut(StatutTache statut) {
        this.statut = Objects.requireNonNull(statut);
    }

    @PrePersist
    void avantCreation() {
        dateCreation = Instant.now();
        dateModification = dateCreation;
    }

    @PreUpdate
    void avantModification() {
        dateModification = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public UUID getDossierId() { return dossierId; }
    public UUID getResponsableId() { return responsableId; }
    public StatutTache getStatut() { return statut; }
    public PrioriteTache getPriorite() { return priorite; }
    public LocalDate getEcheance() { return echeance; }
    public Instant getDateCreation() { return dateCreation; }
    public Instant getDateModification() { return dateModification; }
}
