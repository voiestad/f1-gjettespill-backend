package no.voiestad.f1.placement.leaguePlacementCategory;

import no.voiestad.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface LeaguePlacementCategoryYearStartRepository extends JpaRepository<LeaguePlacementCategoryYearStartEntity, LeaguePlacementCategoryYearStartId> {
    List<LeaguePlacementCategoryYearStartEntity> findByIdYearAndIdUserIdAndIdLeagueId(Year year, UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdUserIdAndIdLeagueId(UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdLeagueId(UUID leagueId);
}
