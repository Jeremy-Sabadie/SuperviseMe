package fr.superviseme.api.dossier;

import fr.superviseme.api.auth.RoleUtilisateur;
import fr.superviseme.api.auth.Utilisateur;
import fr.superviseme.api.auth.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
 * Tests d'intégration des permissions et affectations US-005.1.
 *
 * Utilise PostgreSQL et des JWT réels pour vérifier les accès HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DossierPermissionsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DossierRepository dossiers;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper json;

    /**
     * Crée un utilisateur et récupère son JWT via l'API.
     */
    private Compte creerCompte(RoleUtilisateur role) throws Exception {
        String email = "permissions-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        Utilisateur utilisateur = utilisateurs.save(
                new Utilisateur(
                        email,
                        passwordEncoder.encode(motDePasse),
                        role
                )
        );

        String reponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", email,
                                "motDePasse", motDePasse
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = json.readTree(reponse)
                .get("accessToken")
                .asText();

        return new Compte(utilisateur.getId(), token);
    }

    private record Compte(UUID id, String token) {

        String authorization() {
            return "Bearer " + token;
        }
    }

    /**
     * Un administrateur peut créer un dossier avec un responsable existant.
     */
    @Test
    void adminPeutCreerDossierAvecResponsable() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);

        mvc.perform(post("/api/dossiers")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Dossier sécurisé",
                                "responsableId", responsable.id().toString()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsableId")
                        .value(responsable.id().toString()));
    }

    /**
     * Un nouveau dossier doit obligatoirement avoir un responsable.
     */
    @Test
    void creationSansResponsableRefusee() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);

        mvc.perform(post("/api/dossiers")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titre":"Dossier sans responsable"}
                                """))
                .andExpect(status().isBadRequest());
    }

    /**
     * Un identifiant de responsable inconnu est rejeté.
     */
    @Test
    void responsableInexistantRefuse() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);

        mvc.perform(post("/api/dossiers")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Dossier invalide",
                                "responsableId", UUID.randomUUID().toString()
                        ))))
                .andExpect(status().isNotFound());
    }

    /**
     * Un utilisateur standard ne peut pas créer de dossier.
     */
    @Test
    void utilisateurNePeutPasCreerDossier() throws Exception {
        Compte utilisateur = creerCompte(RoleUtilisateur.USER);

        mvc.perform(post("/api/dossiers")
                        .header("Authorization", utilisateur.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titre":"Création interdite"}
                                """))
                .andExpect(status().isForbidden());
    }

    /**
     * Un utilisateur non affecté ne doit pas connaître
     * l'existence d'un dossier.
     */
    @Test
    void utilisateurNonAffecteNePeutPasConsulterDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte inconnu = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(get("/api/dossiers/{id}", dossierId)
                        .header("Authorization", inconnu.authorization()))
                .andExpect(status().isNotFound());
    }

    /**
     * Le responsable principal peut consulter son dossier.
     */
    @Test
    void responsablePeutConsulterDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(get("/api/dossiers/{id}", dossierId)
                        .header("Authorization", responsable.authorization()))
                .andExpect(status().isOk());
    }

    /**
     * Un utilisateur standard ne peut jamais supprimer un dossier.
     */
    @Test
    void utilisateurNePeutPasSupprimerDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(delete("/api/dossiers/{id}", dossierId)
                        .header("Authorization", responsable.authorization()))
                .andExpect(status().isForbidden());
    }

    /**
     * L'administrateur conserve le droit de supprimer un dossier.
     */
    @Test
    void adminPeutSupprimerDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(delete("/api/dossiers/{id}", dossierId)
                        .header("Authorization", admin.authorization()))
                .andExpect(status().isNoContent());
    }


    /** Le responsable principal peut modifier son dossier. */
    @Test
    void responsablePeutModifierDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(put("/api/dossiers/{id}", dossierId)
                        .header("Authorization", responsable.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titre": "Dossier modifié",
                                  "statut": "EN_COURS",
                                  "priorite": "HAUTE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Dossier modifié"));
    }

    /** Un utilisateur non affecté ne peut pas modifier un dossier. */
    @Test
    void utilisateurNonAffecteNePeutPasModifierDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte inconnu = creerCompte(RoleUtilisateur.USER);

        String dossierId = creerDossier(admin, responsable.id());

        mvc.perform(put("/api/dossiers/{id}", dossierId)
                        .header("Authorization", inconnu.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titre": "Modification interdite",
                                  "statut": "EN_COURS",
                                  "priorite": "NORMALE"
                                }
                                """))
                .andExpect(status().isNotFound());
    }


    /** Un participant affecté peut consulter le dossier. */
    @Test
    void participantPeutConsulterDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte participant = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable.id());

        affecterParticipants(admin, id, participant.id());

        mvc.perform(get("/api/dossiers/{id}", id)
                        .header("Authorization", participant.authorization()))
                .andExpect(status().isOk());
    }

    /** Un participant ne peut pas modifier le dossier. */
    @Test
    void participantNePeutPasModifierDossier() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte participant = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable.id());

        affecterParticipants(admin, id, participant.id());

        mvc.perform(put("/api/dossiers/{id}", id)
                        .header("Authorization", participant.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titre": "Modification interdite",
                                  "statut": "EN_COURS",
                                  "priorite": "NORMALE"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    /** Seul ADMIN peut affecter des participants. */
    @Test
    void utilisateurNePeutPasAffecterParticipants() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);
        Compte responsable = creerCompte(RoleUtilisateur.USER);
        Compte participant = creerCompte(RoleUtilisateur.USER);
        String id = creerDossier(admin, responsable.id());

        mvc.perform(put("/api/dossiers/{id}/participants", id)
                        .header("Authorization", responsable.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "participantIds",
                                java.util.List.of(participant.id())
                        ))))
                .andExpect(status().isForbidden());
    }

    /** Affectation des participants par un administrateur. */
    private void affecterParticipants(
            Compte admin, String dossierId, UUID participantId
    ) throws Exception {
        mvc.perform(put("/api/dossiers/{id}/participants", dossierId)
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "participantIds",
                                java.util.List.of(participantId)
                        ))))
                .andExpect(status().isOk());
    }



    /** Un dossier historique sans responsable reste accessible à ADMIN. */
    @Test
    void adminPeutConsulterAncienDossierSansResponsable() throws Exception {
        Compte admin = creerCompte(RoleUtilisateur.ADMIN);

        Dossier ancien = dossiers.save(
                new Dossier("Ancien dossier", "Créé avant US-005.1")
        );

        mvc.perform(get("/api/dossiers/{id}", ancien.getId())
                        .header("Authorization", admin.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Ancien dossier"));
    }

    /**
     * Crée un dossier avec son responsable et retourne son identifiant.
     */
    private String creerDossier(Compte admin, UUID responsableId)
            throws Exception {

        String reponse = mvc.perform(post("/api/dossiers")
                        .header("Authorization", admin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "titre", "Dossier affecté",
                                "responsableId", responsableId.toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return json.readTree(reponse).get("id").asText();
    }
}
