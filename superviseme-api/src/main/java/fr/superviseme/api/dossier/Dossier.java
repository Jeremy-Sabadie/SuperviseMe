package fr.superviseme.api.dossier;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dossiers")
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutDossier statut = StatutDossier.A_TRAITER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioriteDossier priorite = PrioriteDossier.NORMALE;

    private UUID clientId;

    private UUID responsableId;

    private LocalDate echeance;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(nullable = false)
    private Instant dateModification;

    protected Dossier() {
    }

    public Dossier(String titre, String description) {
        if (titre == null || titre.isBlank() || titre.trim().length() > 200) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        this.titre = titre.trim();
        this.description = description;
    }


    public void modifier(
            String titre,
            String description,
            StatutDossier statut,
            PrioriteDossier priorite,
            LocalDate echeance) {

        if (titre == null || titre.isBlank() || titre.trim().length() > 200) {
            throw new IllegalArgumentException("Titre invalide");
        }

        this.titre = titre.trim();
        this.description = description;
        this.statut = java.util.Objects.requireNonNull(statut);
        this.priorite = java.util.Objects.requireNonNull(priorite);
        this.echeance = echeance;
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
    public StatutDossier getStatut() { return statut; }
    public PrioriteDossier getPriorite() { return priorite; }
    public UUID getClientId() { return clientId; }
    public UUID getResponsableId() { return responsableId; }
    public LocalDate getEcheance() { return echeance; }
    public Instant getDateCreation() { return dateCreation; }
    public Instant getDateModification() { return dateModification; }
}
