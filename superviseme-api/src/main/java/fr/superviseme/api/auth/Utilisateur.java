package fr.superviseme.api.auth;

import jakarta.persistence.*;
import java.util.UUID;

/** Compte utilisateur d'une instance SuperviseMe cliente. */
@Entity
@Table(name = "utilisateurs", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 100)
    private String prenom;

    @Column(length = 100)
    private String nom;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role;

    /**
     * Un compte désactivé reste enregistré pour conserver
     * l'historique des dossiers et des affectations.
     */
    @Column(nullable = false)
    private boolean actif = true;

    protected Utilisateur() {
    }

    public Utilisateur(String email, String motDePasseHash, RoleUtilisateur role) {
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getPrenom() { return prenom; }
    public String getNom() { return nom; }

    public void modifierIdentite(String prenom, String nom) {
        this.prenom = prenom;
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public boolean isActif() {
        return actif;
    }

    /** Bloque le compte sans supprimer ses données historiques. */
    public void modifierEmail(String email) {
        this.email = email;
    }

    public void desactiver() {
        this.actif = false;
    }

    /** Rétablit l'accès au compte existant. */
    public void reactiver() {
        this.actif = true;
    }
}
