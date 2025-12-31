package no.voiestad.f1.placement.leaguePlacementCategory;

import no.voiestad.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface LeaguePlacementCategoryRepository extends JpaRepository<LeaguePlacementCategoryEntity, LeaguePlacementCategoryId> {
    List<LeaguePlacementCategoryEntity> findByIdRaceIdAndIdUserIdAndIdLeagueId(RaceId raceId, UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdUserIdAndIdLeagueId(UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdLeagueId(UUID leagueId);

}
