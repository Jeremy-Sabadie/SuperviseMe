package fr.superviseme.api.agenda;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(length = 255)
    private String lieu;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean partage = false;

    private UUID dossierId;
    private UUID clientId;

    protected RendezVous() {
    }

    public RendezVous(
            String titre,
            LocalDate date,
            LocalTime heureDebut,
            LocalTime heureFin) {

        modifier(titre, date, heureDebut, heureFin);
    }

    public void modifier(
            String titre,
            LocalDate date,
            LocalTime heureDebut,
            LocalTime heureFin) {

        if (titre == null || titre.isBlank() || titre.trim().length() > 200) {
            throw new IllegalArgumentException("Titre invalide");
        }

        if (date == null || heureDebut == null || heureFin == null
                || !heureFin.isAfter(heureDebut)) {
            throw new IllegalArgumentException("Date ou horaires invalides");
        }

        this.titre = titre.trim();
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public boolean estVisiblePar(String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }

        return "USER".equals(role) && partage;
    }

    public void setPartage(boolean partage) {
        this.partage = partage;
    }

    public boolean isPartage() {
        return partage;
    }

    public UUID getId() { return id; }
    public String getTitre() { return titre; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getLieu() { return lieu; }
    public String getDescription() { return description; }
    public UUID getDossierId() { return dossierId; }
    public UUID getClientId() { return clientId; }

    public void definirDetails(
            String lieu,
            String description,
            UUID dossierId,
            UUID clientId) {
        this.lieu = lieu;
        this.description = description;
        this.dossierId = dossierId;
        this.clientId = clientId;
    }
}
