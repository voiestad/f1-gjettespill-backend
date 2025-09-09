package no.vebb.f1.race;

import no.vebb.f1.util.domainPrimitive.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<RaceEntity, RaceId> {
}
