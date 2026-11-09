package fr.superviseme.api.configuration.statut;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Statut métier configurable par le propriétaire d'une entreprise.
 *
 * Chaque instance cliente disposant de sa propre base PostgreSQL,
 * les statuts sont isolés entre entreprises.
 *
 * L'identifiant reste stable même si le libellé change.
 */
@Entity
@Table(name = "statuts_personnalises")
public class StatutPersonnalise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CategorieStatut categorie;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(nullable = false, length = 7)
    private String couleur;

    @Column(name = "ordre_affichage", nullable = false)
    private int ordre;

    /** Constructeur requis par JPA. */
    protected StatutPersonnalise() {
    }

    /**
     * Crée un statut personnalisé après validation des données.
     *
     * @throws IllegalArgumentException si une donnée est invalide
     */
    public StatutPersonnalise(
            CategorieStatut categorie,
            String libelle,
            String couleur,
            int ordre
    ) {
        if (categorie == null) {
            throw new IllegalArgumentException("Catégorie obligatoire");
        }
        if (libelle == null || libelle.isBlank() || libelle.trim().length() > 100) {
            throw new IllegalArgumentException("Libellé invalide");
        }
        if (couleur == null || !couleur.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Couleur hexadécimale invalide");
        }
        if (ordre < 0) {
            throw new IllegalArgumentException("Ordre invalide");
        }

        this.categorie = categorie;
        this.libelle = libelle.trim();
        this.couleur = couleur;
        this.ordre = ordre;
    }

    public UUID getId() { return id; }
    public CategorieStatut getCategorie() { return categorie; }
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }
    public int getOrdre() { return ordre; }
}
