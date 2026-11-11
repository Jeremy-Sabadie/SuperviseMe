package fr.superviseme.api.agenda;

import fr.superviseme.api.client.ClientRepository;
import fr.superviseme.api.dossier.DossierRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RendezVousService {

    private final RendezVousRepository repository;
    private final DossierRepository dossiers;
    private final ClientRepository clients;

    public RendezVousService(
            RendezVousRepository repository,
            DossierRepository dossiers,
            ClientRepository clients) {
        this.repository = repository;
        this.dossiers = dossiers;
        this.clients = clients;
    }

    private boolean estAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void exigerAdmin() {
        if (!estAdmin()) {
            throw new AccessDeniedException("Administration requise");
        }
    }

    @Transactional(readOnly = true)
    public List<RendezVous> lister() {
        return estAdmin()
                ? repository.findAllByOrderByDateAscHeureDebutAsc()
                : repository.findByPartageTrueOrderByDateAscHeureDebutAsc();
    }

    @Transactional(readOnly = true)
    public RendezVous consulter(UUID id) {
        RendezVous rdv = repository.findById(id)
                .orElseThrow(() -> new RendezVousIntrouvableException());

        if (!estAdmin() && !rdv.isPartage()) {
            throw new RendezVousIntrouvableException();
        }
        return rdv;
    }

    public RendezVous creer(RendezVousRequest demande) {
        exigerAdmin();
        verifierReferences(demande);

        RendezVous rdv = new RendezVous(
                demande.titre(), demande.date(),
                demande.heureDebut(), demande.heureFin());

        appliquerDetails(rdv, demande);
        return repository.save(rdv);
    }

    public RendezVous modifier(UUID id, RendezVousRequest demande) {
        exigerAdmin();
        RendezVous rdv = consulter(id);
        verifierReferences(demande);

        rdv.modifier(
                demande.titre(), demande.date(),
                demande.heureDebut(), demande.heureFin());

        appliquerDetails(rdv, demande);
        return rdv;
    }

    public void supprimer(UUID id) {
        exigerAdmin();
        RendezVous rdv = consulter(id);
        repository.delete(rdv);
    }

    private void appliquerDetails(RendezVous rdv, RendezVousRequest demande) {
        rdv.setPartage(demande.partage());
        rdv.definirDetails(
                demande.lieu(), demande.description(),
                demande.dossierId(), demande.clientId());
    }

    private void verifierReferences(RendezVousRequest demande) {
        if (demande.dossierId() != null
                && !dossiers.existsById(demande.dossierId())) {
            throw new IllegalArgumentException("Dossier inexistant");
        }

        if (demande.clientId() != null
                && !clients.existsById(demande.clientId())) {
            throw new IllegalArgumentException("Client inexistant");
        }

        if (demande.dossierId() != null && demande.clientId() != null) {
            var dossier = dossiers.findById(demande.dossierId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Dossier inexistant"));

            if (!demande.clientId().equals(dossier.getClientId())) {
                throw new IllegalArgumentException(
                        "Le dossier ne correspond pas au client");
            }
        }
    }
}
