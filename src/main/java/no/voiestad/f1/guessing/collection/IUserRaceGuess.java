package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.results.domain.CompetitorPosition;

public interface IUserRaceGuess {
    String getUsername();
    String getDriverName();
    CompetitorPosition getStartPosition();
}
