package fr.superviseme.api.demandecontact;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de la gestion des demandes clients.
 *
 * Vérifie le parcours métier complet :
 * - création d'une demande depuis le formulaire public ;
 * - consultation des demandes par un administrateur authentifié ;
 * - changement de statut d'une demande ;
 * - comptabilisation des seules demandes A_TRAITER.
 *
 * Les tests utilisent PostgreSQL et de véritables JWT.
 * Les modifications en base sont annulées après chaque test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestionDemandesIntegrationTest {

    private static final String API = "/api/demandes-clients";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper json;

    /**
     * Crée un administrateur temporaire et récupère son JWT.
     *
     * L'authentification passe par l'API réelle afin de vérifier
     * le comportement avec les mêmes mécanismes qu'en production.
     */
    private String obtenirToken() throws Exception {
        String email = "gestion-" + UUID.randomUUID() + "@test.local";
        String motDePasse = "MotDePasseTest123!";

        utilisateurs.save(new Utilisateur(
                email,
                passwordEncoder.encode(motDePasse),
                RoleUtilisateur.ADMIN
        ));

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

        return json.readTree(reponse)
                .get("accessToken")
                .asText();
    }

    /**
     * Vérifie que le compteur représente uniquement les demandes
     * qui n'ont pas encore été prises en charge.
     *
     * Une demande EN_COURS ou TRAITE ne doit pas déclencher
     * la pastille rouge du tableau de bord.
     */
    @Test
    void compteurNeCompteQueLesDemandesATraiter() throws Exception {

        // ARRANGE : authentifier un administrateur et relever
        // le compteur initial pour ne pas dépendre des données existantes.
        String token = obtenirToken();
        String email = "prospect-" + UUID.randomUUID() + "@test.local";

        long compteurInitial = lireCompteur(token);

        // ACT : simuler l'envoi d'une nouvelle demande par un visiteur.
        mvc.perform(post("/api/public/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "nom", "Prospect test",
                        "email", email,
                        "motif", "Demande de devis",
                        "message", "Création d'une demande de test"
                ))))
                .andExpect(status().isCreated());

        // Retrouver la demande créée via l'API protégée.
        // L'identifiant est récupéré dynamiquement.
        String liste = mvc.perform(get(API)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = null;

        for (var demande : json.readTree(liste)) {
            if (email.equals(demande.get("email").asText())) {
                id = demande.get("id").asText();
                break;
            }
        }

        assertNotNull(id, "La demande créée doit être consultable");

        // ASSERT : une nouvelle demande A_TRAITER augmente le compteur.
        verifierCompteur(token, compteurInitial + 1);

        // EN_COURS : la demande est prise en charge.
        // Elle ne doit plus apparaître dans la notification.
        modifierStatut(token, id, "EN_COURS");
        verifierCompteur(token, compteurInitial);

        // TRAITE : la demande est terminée.
        // Le compteur doit rester inchangé.
        modifierStatut(token, id, "TRAITE");
        verifierCompteur(token, compteurInitial);

        // A_TRAITER : si la demande est remise en attente,
        // elle doit à nouveau être comptabilisée.
        modifierStatut(token, id, "A_TRAITER");
        verifierCompteur(token, compteurInitial + 1);
    }

    /**
     * Modifie le statut d'une demande via l'API authentifiée.
     *
     * Vérifie également que le statut retourné correspond
     * à celui demandé.
     */
    private void modifierStatut(String token, String id, String statut)
            throws Exception {

        mvc.perform(patch(API + "/{id}/statut", id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "statut", statut
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(statut));
    }

    /**
     * Lit le compteur réel des demandes nécessitant un traitement.
     */
    private long lireCompteur(String token) throws Exception {
        String reponse = mvc.perform(get(API + "/compteur")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return Long.parseLong(reponse);
    }

    /**
     * Vérifie la valeur exacte du compteur après une opération.
     */
    private void verifierCompteur(String token, long attendu)
            throws Exception {

        mvc.perform(get(API + "/compteur")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(attendu)));
    }
}
