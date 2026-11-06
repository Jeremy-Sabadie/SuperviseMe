package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurDesactivationTest {

    private Utilisateur collaborateur() {
        return new Utilisateur(
                "collaborateur@exemple.fr",
                "hash-test",
                RoleUtilisateur.USER
        );
    }

    @Test
    void nouveauCollaborateurEstActifParDefaut() {
        assertThat(collaborateur().isActif()).isTrue();
    }

    @Test
    void desactivationBloqueLeCompteSansLeSupprimer() {
        Utilisateur utilisateur = collaborateur();

        utilisateur.desactiver();

        assertThat(utilisateur.isActif()).isFalse();
        assertThat(utilisateur.getEmail())
                .isEqualTo("collaborateur@exemple.fr");
    }

    @Test
    void compteDesactivePeutEtreReactive() {
        Utilisateur utilisateur = collaborateur();

        utilisateur.desactiver();
        utilisateur.reactiver();

        assertThat(utilisateur.isActif()).isTrue();
    }
}
