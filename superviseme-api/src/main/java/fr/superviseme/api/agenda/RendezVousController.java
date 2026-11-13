package fr.superviseme.api.agenda;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agenda")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class RendezVousController {

    private final RendezVousService service;

    public RendezVousController(RendezVousService service) {
        this.service = service;
    }

    @GetMapping
    public List<RendezVousResponse> lister() {
        return service.lister().stream()
                .map(RendezVousResponse::depuis)
                .toList();
    }

    @GetMapping("/{id}")
    public RendezVousResponse consulter(@PathVariable UUID id) {
        return RendezVousResponse.depuis(service.consulter(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RendezVousResponse creer(
            @Valid @RequestBody RendezVousRequest demande) {
        return RendezVousResponse.depuis(service.creer(demande));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RendezVousResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody RendezVousRequest demande) {
        return RendezVousResponse.depuis(service.modifier(id, demande));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }

    @ExceptionHandler(RendezVousIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void introuvable() {
        // Ne révèle pas l'existence d'un rendez-vous privé.
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void donneesInvalides() {
        // Ne divulgue pas de détails internes.
    }
}
