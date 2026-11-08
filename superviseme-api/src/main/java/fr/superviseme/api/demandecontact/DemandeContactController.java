package fr.superviseme.api.demandecontact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Point d'entrée public pour les demandes commerciales.
 *
 * Aucun dossier n'est créé automatiquement.
 */
@RestController
@RequestMapping("/api/public/contact")
public class DemandeContactController {

    private final DemandeContactService service;

    public DemandeContactController(DemandeContactService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void recevoir(@Valid @RequestBody ContactRequest request) {
        service.recevoir(
                request.nom(),
                request.email(),
                request.motif(),
                request.message()
        );
    }

    /** Données acceptées depuis le formulaire public. */
    public record ContactRequest(
            @NotBlank @Size(max = 150) String nom,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 150) String motif,
            @NotBlank @Size(max = 5000) String message
    ) {}
}
