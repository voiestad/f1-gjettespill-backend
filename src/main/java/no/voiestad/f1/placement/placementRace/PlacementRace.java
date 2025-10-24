package no.voiestad.f1.placement.placementRace;

import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.placement.domain.UserPosition;

public interface PlacementRace {
    UserPosition placement();
    UserPoints points();
}
