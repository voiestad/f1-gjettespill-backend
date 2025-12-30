package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface LeaguePlacementRaceYearStartRepository extends JpaRepository<LeaguePlacementRaceYearStartEntity, LeaguePlacementRaceYearStartId> {
    Optional<LeaguePlacementRaceYearStartEntity> findByIdYearAndIdUserIdAndIdLeagueId(Year year, UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdUserIdAndIdLeagueId(UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdLeagueId(UUID leagueId);
}
