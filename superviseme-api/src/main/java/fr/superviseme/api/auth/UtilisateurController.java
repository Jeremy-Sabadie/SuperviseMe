package fr.superviseme.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    public record CollaborateurRequest(
            @NotBlank String email,
            @NotBlank String motDePasse
    ) {}

    public record CollaborateurResponse(
            UUID id,
            String email,
            RoleUtilisateur role
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CollaborateurResponse creer(@RequestBody JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("Corps JSON invalide");
        }

        Set<String> champsAutorises = Set.of("email", "motDePasse");
        var champs = body.propertyNames();

        for (String champ : champs) {
            if (!champsAutorises.contains(champ)) {
                throw new IllegalArgumentException("Champ non autorisé");
            }
        }

        String email = texte(body, "email");
        String motDePasse = texte(body, "motDePasse");

        Utilisateur utilisateur =
                service.creerCollaborateur(email, motDePasse);

        return new CollaborateurResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole()
        );
    }

    private String texte(JsonNode body, String champ) {
        JsonNode valeur = body.get(champ);

        if (valeur == null || !valeur.isString()) {
            throw new IllegalArgumentException("Champ invalide : " + champ);
        }

        return valeur.asText();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> erreurValidation(IllegalArgumentException e) {
        return Map.of("erreur", e.getMessage());
    }
}
