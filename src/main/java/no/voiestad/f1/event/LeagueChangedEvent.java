package no.voiestad.f1.event;

import java.util.UUID;

import no.voiestad.f1.year.Year;

public record LeagueChangedEvent(UUID leagueId, Year year) {
}
