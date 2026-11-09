package fr.superviseme.api.realisation;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class RealisationController {
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private final RealisationRepository repository;

    public RealisationController(RealisationRepository repository) {
        this.repository = repository;
    }

    public record RealisationResponse(
        UUID id, String titre, String description, int position, String imageUrl
    ) {}

    private RealisationResponse toResponse(Realisation r) {
        return new RealisationResponse(
            r.getId(), r.getTitre(), r.getDescription(), r.getPosition(),
            "/api/public/realisations/" + r.getId() + "/image"
        );
    }

    @GetMapping("/api/public/realisations")
    public List<RealisationResponse> lister() {
        return repository.findAllByOrderByPositionAscCreatedAtDesc()
            .stream().map(this::toResponse).toList();
    }

    @GetMapping("/api/public/realisations/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        Realisation r = trouver(id);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(r.getTypeImage()))
            .header("X-Content-Type-Options", "nosniff")
            .cacheControl(CacheControl.noCache())
            .body(r.getImage());
    }

    @PostMapping(value = "/api/realisations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RealisationResponse ajouter(
        @RequestParam String titre,
        @RequestParam(required = false) String description,
        @RequestParam MultipartFile fichier
    ) throws IOException {
        verifierTexte(titre, description);
        byte[] contenu = fichier.getBytes();

        if (contenu.length == 0 || contenu.length > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Taille de fichier invalide");
        }

        String type = detecterType(contenu);
        Realisation r = repository.save(
            new Realisation(titre.trim(), description, type, contenu)
        );
        return toResponse(r);
    }

    @PutMapping("/api/realisations/{id}")
    public RealisationResponse modifier(
        @PathVariable UUID id,
        @RequestBody ModificationRequest request
    ) {
        verifierTexte(request.titre(), request.description());
        Realisation r = trouver(id);
        r.modifier(request.titre().trim(), request.description(), request.position());
        return toResponse(repository.save(r));
    }

    public record ModificationRequest(String titre, String description, int position) {}

    @DeleteMapping("/api/realisations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    private Realisation trouver(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void verifierTexte(String titre, String description) {
        if (titre == null || titre.isBlank() || titre.length() > 150
            || (description != null && description.length() > 1000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titre ou description invalide");
        }
    }

    private String detecterType(byte[] data) {
        if (data.length >= 8
            && (data[0] & 255) == 137 && data[1] == 80
            && data[2] == 78 && data[3] == 71
            && data[4] == 13 && data[5] == 10
            && data[6] == 26 && data[7] == 10) {
            return "image/png";
        }
        if (data.length >= 4
            && (data[0] & 255) == 255 && (data[1] & 255) == 216
            && (data[2] & 255) == 255) {
            return "image/jpeg";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format image non autorise");
    }
}
