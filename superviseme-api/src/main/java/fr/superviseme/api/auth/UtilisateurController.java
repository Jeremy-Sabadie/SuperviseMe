package fr.superviseme.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            RoleUtilisateur role,
            boolean actif
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
                utilisateur.getRole(),
                utilisateur.isActif()
        );
    }


    /** Liste les comptes pouvant être responsables d'un dossier. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<CollaborateurResponse> lister() {
        return service.lister().stream()
                .map(utilisateur -> new CollaborateurResponse(
                        utilisateur.getId(),
                        utilisateur.getEmail(),
                        utilisateur.getRole(),
                utilisateur.isActif()
                ))
                .toList();
    }

    public record ModifierCollaborateurRequest(@NotBlank String email) {}

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CollaborateurResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody ModifierCollaborateurRequest request) {
        return reponse(service.modifierEmail(id, request.email()));
    }

    /** Désactivation réservée aux administrateurs. */
    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public CollaborateurResponse desactiver(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID administrateurId = UUID.fromString(jwt.getSubject());
        Utilisateur utilisateur = service.desactiver(id, administrateurId);
        return reponse(utilisateur);
    }

    /** Réactivation réservée aux administrateurs. */
    @PatchMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public CollaborateurResponse reactiver(@PathVariable UUID id) {
        return reponse(service.reactiver(id));
    }

    private CollaborateurResponse reponse(Utilisateur utilisateur) {
        return new CollaborateurResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                utilisateur.isActif()
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
