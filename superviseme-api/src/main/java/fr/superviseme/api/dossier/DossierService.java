package fr.superviseme.api.dossier;

import fr.superviseme.api.client.ClientRepository;
import fr.superviseme.api.auth.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Gère le cycle de vie des dossiers.
 * Les règles métier restent ici, indépendamment du protocole HTTP.
 */
@Service
@Transactional
public class DossierService {

    private final DossierRepository repository;
    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DossierService(
            DossierRepository repository,
            ClientRepository clientRepository,
            UtilisateurRepository utilisateurRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Crée un dossier avec ses valeurs métier par défaut. */
    public Dossier creer(CreerDossierRequest demande) {
        verifierClientExistant(demande.clientId());

        UUID responsableId = demande.responsableId();
        if (responsableId == null) {
            throw new IllegalArgumentException("Responsable obligatoire");
        }
        if (!utilisateurRepository.existsById(responsableId)) {
            throw new ResponsableIntrouvableException(responsableId);
        }

        Dossier dossier = new Dossier(
                demande.titre(),
                demande.description()
        );
        dossier.associerClient(demande.clientId());
        dossier.affecterResponsable(responsableId);

        return repository.save(dossier);
    }

    /**
     * Liste uniquement les dossiers accessibles à l'utilisateur connecté.
     * ADMIN conserve une visibilité globale, y compris sur les anciens dossiers.
     */
    @Transactional(readOnly = true)
    public List<Dossier> lister() {
        List<Dossier> dossiers = repository.findAll();

        // Initialiser les participants avant la fermeture de la transaction.
        dossiers.forEach(dossier -> dossier.getParticipantIds().size());

        if (estAdministrateur()) {
            return dossiers;
        }

        UUID utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            // Sans identité valide, aucun dossier ne doit être divulgué.
            return List.of();
        }

        return dossiers.stream()
                .filter(dossier ->
                        utilisateurId.equals(dossier.getResponsableId())
                        || dossier.getParticipantIds().contains(utilisateurId))
                .toList();
    }

    /**
     * Consulte un dossier uniquement si l'utilisateur y est affecté.
     * Un dossier non autorisé retourne 404 pour masquer son existence.
     */
    @Transactional(readOnly = true)
    public Dossier consulter(UUID id) {
        Dossier dossier = repository.findById(id)
                .orElseThrow(() -> new DossierIntrouvableException(id));

        // Initialiser les participants avant la fermeture de la transaction.
        dossier.getParticipantIds().size();

        if (estAdministrateur()) {
            return dossier;
        }

        UUID utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            // Masque également l'existence du dossier.
            throw new DossierIntrouvableException(id);
        }

        if (!utilisateurId.equals(dossier.getResponsableId())
                && !dossier.getParticipantIds().contains(utilisateurId)) {
            throw new DossierIntrouvableException(id);
        }

        return dossier;
    }

    /** Vérifie si l'identité authentifiée possède le rôle ADMIN. */
    private boolean estAdministrateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()));
    }

    /**
     * Extrait l'identifiant UUID du JWT.
     * Retourne null uniquement en l'absence de contexte authentifié.
     */
    private UUID utilisateurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Autorise la modification à ADMIN et au responsable du dossier.
     * Un participant peut consulter le dossier, mais pas le modifier.
     * Un utilisateur non affecté ne doit pas connaître son existence.
     */
    public Dossier modifier(UUID id, ModifierDossierRequest demande) {
        Dossier dossier = consulter(id);

        if (!estAdministrateur()) {
            UUID utilisateurId = utilisateurConnecteId();

            if (!utilisateurId.equals(dossier.getResponsableId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Modification réservée au responsable du dossier");
            }
        }

        verifierClientExistant(demande.clientId());

        dossier.modifier(
                demande.titre(),
                demande.description(),
                demande.statut(),
                demande.priorite(),
                demande.echeance()
        );
        dossier.associerClient(demande.clientId());

        return dossier;
    }

    /**
     * Déplace un dossier dans le Kanban.
     * ADMIN et responsable peuvent modifier son statut.
     * Les autres utilisateurs conservent les protections de consultation.
     */
    public Dossier changerStatut(UUID id, StatutDossier statut) {
        Dossier dossier = consulter(id);

        if (!estAdministrateur()) {
            UUID utilisateurId = utilisateurConnecteId();

            if (!java.util.Objects.equals(
                    utilisateurId, dossier.getResponsableId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Déplacement réservé au responsable du dossier");
            }
        }

        dossier.changerStatut(statut);

        // Charger les participants avant la fermeture de la transaction.
        dossier.getParticipantIds().size();

        return dossier;
    }

    /**
     * Vérifie l'existence d'un client avant de l'associer à un dossier.
     * Une association absente est autorisée.
     */
    private void verifierClientExistant(UUID clientId) {
        if (clientId != null && !clientRepository.existsById(clientId)) {
            throw new ClientDossierIntrouvableException(clientId);
        }
    }

    /**
     * Remplace les participants d'un dossier.
     * Vérifie tous les identifiants avant toute modification.
     * L'autorisation ADMIN est également contrôlée dans le contrôleur.
     */
    public Dossier affecterParticipants(UUID id, java.util.Set<UUID> participantIds) {
        if (!estAdministrateur()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Affectation réservée aux administrateurs");
        }

        Dossier dossier = consulter(id);

        if (participantIds == null || participantIds.contains(null)) {
            throw new IllegalArgumentException("Participants invalides");
        }

        for (UUID participantId : participantIds) {
            if (!utilisateurRepository.existsById(participantId)) {
                throw new ResponsableIntrouvableException(participantId);
            }
        }

        dossier.affecterParticipants(participantIds);
        return dossier;
    }

    /** Supprime un dossier existant. */
    public void supprimer(UUID id) {
        repository.delete(consulter(id));
    }
}
