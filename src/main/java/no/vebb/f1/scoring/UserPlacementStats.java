package no.vebb.f1.scoring;

import no.vebb.f1.placement.collection.Medals;
import no.vebb.f1.collection.Placement;
import no.vebb.f1.year.Year;

import java.util.List;

public record UserPlacementStats(String username, List<Placement<Year>> previousPlacements, Medals medals) {}
