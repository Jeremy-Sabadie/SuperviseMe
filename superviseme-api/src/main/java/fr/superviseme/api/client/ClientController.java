package fr.superviseme.api.client;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API REST de gestion des clients d'une entreprise.
 */
@RestController
@RequestMapping("/api/clients")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    /** POST : création d'un client. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse creer(@Valid @RequestBody ClientRequest request) {
        return service.creer(request);
    }

    /** GET : liste ou recherche par nom. */
    @GetMapping
    public List<ClientResponse> lister(
            @RequestParam(required = false) String recherche) {
        return service.lister(recherche);
    }

    /** GET : consultation d'un client. */
    @GetMapping("/{id}")
    public ClientResponse consulter(@PathVariable UUID id) {
        return service.consulter(id);
    }

    /** PUT : modification d'un client. */
    @PutMapping("/{id}")
    public ClientResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request) {
        return service.modifier(id, request);
    }

    /** PATCH : changement du statut personnalisé d'un client. */
    @PatchMapping("/{id}/statut-personnalise")
    public ClientResponse changerStatutPersonnalise(
            @PathVariable UUID id,
            @Valid @RequestBody ChangerStatutClientRequest request) {
        return service.changerStatutPersonnalise(id, request.statutId());
    }

    /** DELETE : suppression d'un client non référencé. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }

    /** Retourne 404 lorsqu'un client n'existe pas. */
    @ExceptionHandler(ClientIntrouvableException.class)
    public ResponseEntity<String> clientIntrouvable(
            ClientIntrouvableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    /** Retourne 409 lorsqu'un client possède des dossiers. */
    @ExceptionHandler(ClientAvecDossiersException.class)
    public ResponseEntity<String> clientAvecDossiers(
            ClientAvecDossiersException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    /** Protection complémentaire contre les violations de contraintes SQL. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> clientReference() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Impossible de supprimer ce client : des dossiers lui sont associés.");
    }
}
