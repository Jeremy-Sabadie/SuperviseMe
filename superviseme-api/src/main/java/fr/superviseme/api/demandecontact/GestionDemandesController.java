package fr.superviseme.api.demandecontact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/demandes-clients")
public class GestionDemandesController {

    private final DemandeContactRepository repository;

    public GestionDemandesController(DemandeContactRepository repository) {
        this.repository = repository;
    }

    public record DemandeResponse(
            UUID id,
            UUID clientId,
            String nom,
            String email,
            String motif,
            String message,
            DemandeContact.Statut statut,
            Instant dateCreation
    ) {}

    public record StatutRequest(
            @NotNull DemandeContact.Statut statut
    ) {}

    private DemandeResponse convertir(DemandeContact demande) {
        return new DemandeResponse(
                demande.getId(),
                demande.getClient().getId(),
                demande.getClient().getNom(),
                demande.getClient().getEmail(),
                demande.getMotif(),
                demande.getMessage(),
                demande.getStatut(),
                demande.getDateCreation()
        );
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<DemandeResponse> lister() {
        return repository.findAll().stream()
                .sorted((a, b) ->
                        b.getDateCreation().compareTo(a.getDateCreation()))
                .map(this::convertir)
                .toList();
    }

    @GetMapping("/compteur")
    public long compteur() {
        return repository.findAll().stream()
                .filter(d -> d.getStatut() == DemandeContact.Statut.A_TRAITER)
                .count();
    }

    @PatchMapping("/{id}/statut")
    @Transactional
    public ResponseEntity<DemandeResponse> changerStatut(
            @PathVariable UUID id,
            @Valid @RequestBody StatutRequest request) {

        return repository.findById(id)
                .map(demande -> {
                    demande.changerStatut(request.statut());
                    return ResponseEntity.ok(convertir(demande));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
