package no.voiestad.f1.scoring;

import no.voiestad.f1.placement.collection.Medals;
import no.voiestad.f1.collection.Placement;
import no.voiestad.f1.year.Year;

import java.util.List;

public record UserPlacementStats(String username, List<Placement<Year>> previousPlacements, Medals medals) {}
