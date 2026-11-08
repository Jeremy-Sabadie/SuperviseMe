package fr.superviseme.api.tache;

import fr.superviseme.api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Vérifie les permissions HTTP et les erreurs de l'API des tâches.
 *
 * Les contrôles de propriété d'une tâche restent testés
 * dans TacheServiceTest.
 */
@WebMvcTest(TacheController.class)
@Import(SecurityConfig.class)
class TacheControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TacheService service;

    @Test
    void visiteurNePeutPasLister() throws Exception {
        mvc.perform(get("/api/taches"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void utilisateurPeutLister() throws Exception {
        when(service.lister()).thenReturn(List.of());

        mvc.perform(get("/api/taches"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administrateurPeutLister() throws Exception {
        when(service.lister()).thenReturn(List.of());

        mvc.perform(get("/api/taches"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void utilisateurNePeutPasCreer() throws Exception {
        mvc.perform(post("/api/taches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titre":"Préparer commande",
                     "responsableId":"00000000-0000-0000-0000-000000000001"}
                    """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void titreVideRetourne400() throws Exception {
        mvc.perform(post("/api/taches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titre":"",
                     "responsableId":"00000000-0000-0000-0000-000000000001"}
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void statutAbsentRetourne400() throws Exception {
        mvc.perform(patch("/api/taches/{id}/statut", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tacheInexistanteRetourne404() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.changerStatut(id, StatutTache.EN_COURS))
                .thenThrow(new TacheIntrouvableException(id));

        mvc.perform(patch("/api/taches/{id}/statut", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"statut":"EN_COURS"}
                    """))
                .andExpect(status().isNotFound());
    }
}
