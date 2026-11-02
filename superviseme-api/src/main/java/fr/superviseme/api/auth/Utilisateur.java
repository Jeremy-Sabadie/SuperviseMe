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

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role;

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

    public String getEmail() {
        return email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public RoleUtilisateur getRole() {
        return role;
    }
}
