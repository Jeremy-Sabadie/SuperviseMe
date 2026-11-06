package fr.superviseme.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Vérifie les autorisations de désactivation des comptes.
 * Seul un administrateur peut désactiver un collaborateur,
 * sans jamais pouvoir désactiver son propre compte.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UtilisateurDesactivationAutorisationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UtilisateurRepository repository;
    @Autowired PasswordEncoder encoder;

    /** Crée un compte réel et récupère son JWT pour tester les droits HTTP. */
    private String token(Utilisateur utilisateur) throws Exception {
        String reponse = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email", utilisateur.getEmail(),
                        "motDePasse", "MotDePasseTest123!"
                ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(reponse).get("accessToken").asText();
    }

    private Utilisateur creer(RoleUtilisateur role) {
        return repository.saveAndFlush(new Utilisateur(
                UUID.randomUUID() + "@test.local",
                encoder.encode("MotDePasseTest123!"),
                role
        ));
    }

    /** Un utilisateur standard ne peut désactiver aucun collaborateur. */
    @Test
    void collaborateurNePeutPasDesactiverUnCompte() throws Exception {
        Utilisateur acteur = creer(RoleUtilisateur.USER);
        Utilisateur cible = creer(RoleUtilisateur.USER);

        mvc.perform(patch("/api/utilisateurs/{id}/desactiver", cible.getId())
                .header("Authorization", "Bearer " + token(acteur)))
                .andExpect(status().isForbidden());
    }

    /** Empêche un administrateur de perdre son propre accès. */
    @Test
    void administrateurNePeutPasSeDesactiver() throws Exception {
        Utilisateur admin = creer(RoleUtilisateur.ADMIN);

        mvc.perform(patch("/api/utilisateurs/{id}/desactiver", admin.getId())
                .header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isForbidden());
    }

    /** Un administrateur peut désactiver un autre collaborateur. */
    @Test
    void administrateurPeutDesactiverUnCollaborateur() throws Exception {
        Utilisateur admin = creer(RoleUtilisateur.ADMIN);
        Utilisateur cible = creer(RoleUtilisateur.USER);

        mvc.perform(patch("/api/utilisateurs/{id}/desactiver", cible.getId())
                .header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(false));
    }

    /**
     * Un administrateur peut réactiver un ancien collaborateur.
     * Le compte est conservé avec son identité et son historique.
     */
    @Test
    void administrateurPeutReactiverUnCollaborateur() throws Exception {
        Utilisateur admin = creer(RoleUtilisateur.ADMIN);
        Utilisateur cible = creer(RoleUtilisateur.USER);
        cible.desactiver();
        repository.saveAndFlush(cible);

        mvc.perform(patch("/api/utilisateurs/{id}/reactiver", cible.getId())
                .header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(true));
    }

    /**
     * Un collaborateur standard ne peut pas réactiver un compte,
     * même s'il connaît son identifiant.
     */
    @Test
    void collaborateurNePeutPasReactiverUnCompte() throws Exception {
        Utilisateur acteur = creer(RoleUtilisateur.USER);
        Utilisateur cible = creer(RoleUtilisateur.USER);
        cible.desactiver();
        repository.saveAndFlush(cible);

        mvc.perform(patch("/api/utilisateurs/{id}/reactiver", cible.getId())
                .header("Authorization", "Bearer " + token(acteur)))
                .andExpect(status().isForbidden());
    }
}
