package fr.superviseme.api.dossier;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

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

    @Column(name = "responsable_id")
    private UUID responsableId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "dossier_participants",
            joinColumns = @JoinColumn(name = "dossier_id")
    )
    @Column(name = "utilisateur_id", nullable = false)
    private Set<UUID> participantIds = new HashSet<>();

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

    /**
     * Change uniquement le statut du dossier.
     * Les autres propriétés métier restent inchangées.
     */
    public void changerStatut(StatutDossier nouveauStatut) {
        this.statut = java.util.Objects.requireNonNull(nouveauStatut);
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

    /**
     * Associe un client au dossier ou retire l'association si l'identifiant est nul.
     * L'existence du client est vérifiée par le service métier.
     */
    public void associerClient(UUID clientId) {
        this.clientId = clientId;
    }

    /**
     * Affecte le responsable principal.
     * Son existence est vérifiée dans le service métier.
     */
    public void affecterResponsable(UUID responsableId) {
        this.responsableId = java.util.Objects.requireNonNull(
                responsableId, "Responsable obligatoire"
        );
    }

    /**
     * Remplace les participants affectés au dossier.
     * Les identifiants sont validés par le service métier.
     */
    public void affecterParticipants(Set<UUID> participants) {
        java.util.Objects.requireNonNull(participants);
        this.participantIds.clear();
        this.participantIds.addAll(participants);
    }

    /** Retourne une copie non modifiable des participants. */
    public Set<UUID> getParticipantIds() {
        return Collections.unmodifiableSet(
                new HashSet<>(participantIds)
        );
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
