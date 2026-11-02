package fr.superviseme.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** API de connexion des utilisateurs clients. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String motDePasse
    ) {}

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        return Map.of("accessToken",
                service.connecter(request.email(), request.motDePasse()));
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "id", jwt.getSubject(),
                "roles", jwt.getClaimAsStringList("roles")
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> identifiantsIncorrects() {
        return Map.of("erreur", "Identifiants incorrects");
    }
}
