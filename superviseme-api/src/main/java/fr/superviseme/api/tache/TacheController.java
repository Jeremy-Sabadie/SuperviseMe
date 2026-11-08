package fr.superviseme.api.tache;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Points d'entrée REST pour la gestion des tâches.
 *
 * Les règles d'autorisation sont appliquées à deux niveaux :
 * - le contrôleur filtre les rôles autorisés ;
 * - le service vérifie les droits sur chaque tâche.
 *
 * Cette double vérification protège également les appels au service
 * effectués en dehors du contrôleur HTTP.
 */
@RestController
@RequestMapping("/api/taches")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class TacheController {

    private final TacheService service;

    public TacheController(TacheService service) {
        this.service = service;
    }

    /**
     * Crée une tâche indépendante ou associée à un dossier.
     *
     * Seul un administrateur peut créer et affecter une tâche.
     *
     * @param demande données de création validées
     * @return tâche créée
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TacheResponse creer(
            @Valid @RequestBody CreerTacheRequest demande) {

        return TacheResponse.depuis(service.creer(demande));
    }

    /**
     * Retourne toutes les tâches de l'entreprise.
     *
     * ADMIN et USER disposent de la même visibilité.
     * L'isolation entre entreprises repose sur leurs bases dédiées.
     *
     * @return liste des tâches
     */
    @GetMapping
    public List<TacheResponse> lister() {
        return service.lister().stream()
                .map(TacheResponse::depuis)
                .toList();
    }

    /**
     * Modifie exclusivement le statut d'une tâche.
     *
     * ADMIN peut modifier toutes les tâches.
     * USER peut modifier uniquement les tâches dont il est responsable.
     * Le contrôle de propriété est effectué dans TacheService.
     *
     * @param id identifiant de la tâche
     * @param demande nouveau statut validé
     * @return tâche actualisée
     */
    @PatchMapping("/{id}/statut")
    public TacheResponse changerStatut(
            @PathVariable UUID id,
            @Valid @RequestBody ChangerStatutTacheRequest demande) {

        return TacheResponse.depuis(
                service.changerStatut(id, demande.statut()));
    }

    /**
     * Masque les détails internes lorsqu'une tâche est inexistante.
     */
    @ExceptionHandler(TacheIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void tacheIntrouvable(TacheIntrouvableException exception) {
        // Le statut HTTP suffit pour informer le client.
    }
}
