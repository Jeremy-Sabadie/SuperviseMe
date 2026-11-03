package fr.superviseme.api.dossier;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API REST des dossiers.
 * Accessible uniquement aux utilisateurs authentifiés
 * disposant du rôle ADMIN ou USER.
 */
@RestController
@RequestMapping("/api/dossiers")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class DossierController {

    private final DossierService service;

    public DossierController(DossierService service) {
        this.service = service;
    }

    /**
     * Réserve la création des dossiers aux administrateurs.
     * Le responsable est ensuite validé par le service métier.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DossierResponse creer(@Valid @RequestBody CreerDossierRequest demande) {
        return DossierResponse.depuis(service.creer(demande));
    }

    /** GET /api/dossiers : consultation de la liste. */
    @GetMapping
    public List<DossierResponse> lister() {
        return service.lister().stream()
                .map(DossierResponse::depuis)
                .toList();
    }

    /** GET /api/dossiers/{id} : consultation individuelle. */
    @GetMapping("/{id}")
    public DossierResponse consulter(@PathVariable UUID id) {
        return DossierResponse.depuis(service.consulter(id));
    }

    /** PUT /api/dossiers/{id} : modification complète. */
    @PutMapping("/{id}")
    public DossierResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody ModifierDossierRequest demande) {
        return DossierResponse.depuis(service.modifier(id, demande));
    }

    /**
     * Réserve la suppression définitive aux administrateurs.
     * Un responsable ou participant ne peut pas supprimer un dossier.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }

    /**
     * Réserve à ADMIN l'affectation des participants d'un dossier.
     * Remplace intégralement la liste des participants existants.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/participants")
    public DossierResponse affecterParticipants(
            @PathVariable UUID id,
            @Valid @RequestBody AffecterParticipantsRequest demande) {
        return DossierResponse.depuis(
                service.affecterParticipants(id, demande.participantIds()));
    }

    /** Retourne 404 si le client demandé n'existe pas. */
    @ExceptionHandler(ClientDossierIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void clientIntrouvable(ClientDossierIntrouvableException exception) {
        // Aucun détail interne n'est exposé.
    }

    /**
     * Masque les détails internes lorsqu'un responsable demandé
     * n'existe pas dans l'instance cliente.
     */
    @ExceptionHandler(ResponsableIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void responsableIntrouvable(
            ResponsableIntrouvableException exception) {
        // Ne divulgue aucune information sur les comptes utilisateurs.
    }

    /** Convertit une absence de dossier en réponse HTTP 404. */
    @ExceptionHandler(DossierIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void dossierIntrouvable(DossierIntrouvableException exception) {
        // Le statut HTTP suffit ; aucun détail interne n'est exposé.
    }
}
