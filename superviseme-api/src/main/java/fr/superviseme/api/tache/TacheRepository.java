package fr.superviseme.api.tache;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TacheRepository extends JpaRepository<Tache, UUID> {
}
