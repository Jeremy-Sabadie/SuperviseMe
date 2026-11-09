package fr.superviseme.api.realisation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RealisationRepository extends JpaRepository<Realisation, UUID> {
    List<Realisation> findAllByOrderByPositionAscCreatedAtDesc();
}
