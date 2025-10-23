package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.results.domain.CompetitorPosition;

public interface IUserRaceGuessTable {
    DriverName getDriverName();
    CompetitorPosition getStartPosition();
    RacePosition getRacePosition();
    String getRaceName();
    CompetitorPosition getFinishingPosition();
}
