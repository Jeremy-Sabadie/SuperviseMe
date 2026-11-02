package fr.superviseme.api.entreprise;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * API REST d'administration des entreprises clientes.
 *
 * L'accès aux routes /api/admin/** est réservé au rôle ADMIN
 * par la configuration Spring Security.
 */
@RestController
@RequestMapping("/api/admin/entreprises")
public class EntrepriseController {

    private final EntrepriseService service;

    public EntrepriseController(EntrepriseService service) {
        this.service = service;
    }

    /**
     * Données nécessaires à la création d'une entreprise.
     */
    public record CreationEntrepriseRequest(
            @NotBlank @Size(max = 150) String nom,
            @Email @Size(max = 254) String email,
            @Size(max = 30) String telephone
    ) {}

    /**
     * Représentation publique d'une entreprise.
     * Évite d'exposer directement l'entité JPA.
     */
    public record EntrepriseResponse(
            UUID id,
            String nom,
            String email,
            String telephone,
            StatutEntreprise statut
    ) {
        /**
         * Convertit une entité métier en réponse API.
         */
        public static EntrepriseResponse depuis(Entreprise entreprise) {
            return new EntrepriseResponse(
                    entreprise.getId(),
                    entreprise.getNom(),
                    entreprise.getEmail(),
                    entreprise.getTelephone(),
                    entreprise.getStatut()
            );
        }
    }

    /**
     * Crée une entreprise avec le statut initial EN_ATTENTE.
     *
     * @return HTTP 201 avec l'entreprise créée et son URI
     */
    @PostMapping
    public ResponseEntity<EntrepriseResponse> creer(
            @Valid @RequestBody CreationEntrepriseRequest request) {

        Entreprise entreprise = service.creer(
                request.nom(), request.email(), request.telephone());

        return ResponseEntity
                .created(URI.create("/api/admin/entreprises/" + entreprise.getId()))
                .body(EntrepriseResponse.depuis(entreprise));
    }

    /**
     * Consulte une entreprise à partir de son UUID.
     *
     * @return HTTP 200 si elle existe, HTTP 404 sinon
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> rechercher(@PathVariable UUID id) {
        return service.rechercherParId(id)
                .map(EntrepriseResponse::depuis)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
