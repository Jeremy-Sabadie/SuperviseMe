package fr.superviseme.api.realisation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "realisations")
public class Realisation {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Column(name = "type_image", nullable = false, length = 20)
    private String typeImage;

    @Column(nullable = false)
    private byte[] image;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Realisation() {}

    public Realisation(String titre, String description, String typeImage, byte[] image) {
        this.titre = titre;
        this.description = description;
        this.typeImage = typeImage;
        this.image = image;
    }

    public UUID getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getTypeImage() { return typeImage; }
    public byte[] getImage() { return image; }
    public int getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }

    public void modifier(String titre, String description, int position) {
        this.titre = titre;
        this.description = description;
        this.position = position;
    }
}
