package fr.superviseme.api.configuration.statut;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API de configuration des statuts de l'entreprise cliente.
 *
 * La consultation est ouverte aux collaborateurs authentifiés.
 * Les modifications sont réservées à l'administrateur client.
 * La suppression n'est pas exposée avant la protection des références métier.
 */
@RestController
@RequestMapping("/api/admin/statuts")
public class StatutPersonnaliseController {

    private final StatutPersonnaliseService service;

    public StatutPersonnaliseController(StatutPersonnaliseService service) {
        this.service = service;
    }

    /** Liste les statuts d'une catégorie dans leur ordre d'affichage. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<StatutResponse> lister(@RequestParam CategorieStatut categorie) {
        return service.lister(categorie).stream()
                .map(StatutResponse::from)
                .toList();
    }

    /** Ajoute un statut à une catégorie. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public StatutResponse creer(@Valid @RequestBody StatutRequest request) {
        return StatutResponse.from(service.creer(new StatutPersonnalise(
                request.categorie(),
                request.libelle(),
                request.couleur(),
                request.ordre()
        )));
    }

    /** Modifie un statut sans changer son identifiant ni sa catégorie. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatutResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody ModificationStatutRequest request) {
        return StatutResponse.from(service.modifier(
                id, request.libelle(), request.couleur(), request.ordre()
        ));
    }

    /** Change la position d'affichage d'un statut. */
    @PatchMapping("/{id}/ordre")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordonner(
            @PathVariable UUID id,
            @Valid @RequestBody OrdreStatutRequest request) {
        service.reordonner(id, request.ordre());
    }

    /** Transforme les erreurs de validation métier en réponse HTTP 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> erreurMetier(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    public record StatutRequest(
            @NotNull CategorieStatut categorie,
            @NotBlank String libelle,
            @NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String couleur,
            @Min(0) int ordre
    ) {}

    public record ModificationStatutRequest(
            @NotBlank String libelle,
            @NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String couleur,
            @Min(0) int ordre
    ) {}

    public record OrdreStatutRequest(@Min(0) int ordre) {}

    public record StatutResponse(
            UUID id,
            CategorieStatut categorie,
            String libelle,
            String couleur,
            int ordre
    ) {
        public static StatutResponse from(StatutPersonnalise statut) {
            return new StatutResponse(
                    statut.getId(),
                    statut.getCategorie(),
                    statut.getLibelle(),
                    statut.getCouleur(),
                    statut.getOrdre()
            );
        }
    }
}
