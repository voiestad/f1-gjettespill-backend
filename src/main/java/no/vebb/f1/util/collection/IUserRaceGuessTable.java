package no.vebb.f1.util.collection;

import no.vebb.f1.race.RacePosition;

public interface IUserRaceGuessTable {
    String getDriverName();
    int getStartPosition();
    RacePosition getRacePosition();
    String getRaceName();
    int getFinishingPosition();
}
