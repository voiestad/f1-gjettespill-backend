package no.vebb.f1.race;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<RaceEntity, RaceId> {
}
