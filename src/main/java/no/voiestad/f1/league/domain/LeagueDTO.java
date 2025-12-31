package no.voiestad.f1.league.domain;

import java.util.UUID;

import no.voiestad.f1.league.leagues.LeagueEntity;
import no.voiestad.f1.year.Year;

public record LeagueDTO(UUID leagueId, String leagueName, Year year) {
    public static LeagueDTO fromEntity(LeagueEntity entity) {
        return new LeagueDTO(entity.leagueId(), entity.leagueName(), entity.year());
    }
}
