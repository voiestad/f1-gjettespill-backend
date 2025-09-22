package no.vebb.f1.guessing.collection;

import no.vebb.f1.results.domain.CompetitorPosition;

public interface IUserRaceGuess {
    String getUsername();
    String getDriverName();
    CompetitorPosition getStartPosition();
}
