package no.vebb.f1.guessing.collection;

import no.vebb.f1.race.RacePosition;
import no.vebb.f1.results.domain.CompetitorPosition;

public interface IUserRaceGuessTable {
    String getDriverName();
    CompetitorPosition getStartPosition();
    RacePosition getRacePosition();
    String getRaceName();
    CompetitorPosition getFinishingPosition();
}
