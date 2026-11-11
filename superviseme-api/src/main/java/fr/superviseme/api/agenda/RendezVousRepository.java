package fr.superviseme.api.agenda;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RendezVousRepository extends JpaRepository<RendezVous, UUID> {
    List<RendezVous> findAllByOrderByDateAscHeureDebutAsc();
    List<RendezVous> findByPartageTrueOrderByDateAscHeureDebutAsc();
}
