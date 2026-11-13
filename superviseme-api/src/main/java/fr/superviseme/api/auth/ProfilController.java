package fr.superviseme.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * API du profil connecté.
 *
 * Aucun identifiant utilisateur n'est accepté dans l'URL :
 * toutes les opérations ciblent le sujet du JWT authentifié.
 */
@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final ProfilService profilService;
    private final JwtRevocationService revocations;

    public ProfilController(
            ProfilService profilService,
            JwtRevocationService revocations) {
        this.profilService = profilService;
        this.revocations = revocations;
    }

    public record IdentiteRequest(
            String prenom,
            String nom) {}

    public record EmailRequest(
            @NotBlank String email,
            @NotBlank String motDePasseActuel) {}

    public record MotDePasseRequest(
            @NotBlank String motDePasseActuel,
            @NotBlank String nouveauMotDePasse) {}

    @GetMapping
    public ProfilService.Profil consulter(
            @AuthenticationPrincipal Jwt jwt) {
        return profilService.consulter(utilisateurId(jwt));
    }

    @PutMapping("/identite")
    public ProfilService.Profil modifierIdentite(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody IdentiteRequest demande) {

        return profilService.modifierIdentite(
                utilisateurId(jwt),
                demande.prenom(),
                demande.nom());
    }

    @PutMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifierEmail(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EmailRequest demande) {

        profilService.modifierEmail(
                utilisateurId(jwt),
                demande.email(),
                demande.motDePasseActuel());

        // Après une modification sensible, le JWT courant
        // ne doit plus pouvoir être réutilisé.
        revoquerJwt(jwt);
    }

    @PutMapping("/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifierMotDePasse(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MotDePasseRequest demande) {

        profilService.modifierMotDePasse(
                utilisateurId(jwt),
                demande.motDePasseActuel(),
                demande.nouveauMotDePasse());

        revoquerJwt(jwt);
    }

    private UUID utilisateurId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private void revoquerJwt(Jwt jwt) {
        revocations.revoquer(
                jwt.getTokenValue(),
                jwt.getExpiresAt());
    }
}
