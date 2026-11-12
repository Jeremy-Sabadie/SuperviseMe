package fr.superviseme.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** API publique de récupération des accès. */
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    public record Demande(
        @NotBlank @Email String email
    ) {}

    public record Reinitialisation(
        @NotBlank String token,
        @NotBlank @Size(min = 12, max = 128) String motDePasse
    ) {}

    @PostMapping("/forgot-password")
    public Map<String, String> demander(@Valid @RequestBody Demande requete) {
        service.demander(requete.email());
        return Map.of("message",
            "Si un compte correspond à cette adresse, un lien sera envoyé.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> reinitialiser(
            @Valid @RequestBody Reinitialisation requete) {
        service.reinitialiser(requete.token(), requete.motDePasse());
        return Map.of("message", "Mot de passe modifié.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> erreur() {
        return Map.of("erreur", "Lien invalide, expiré ou mot de passe incorrect.");
    }
}
