package no.vebb.f1.placement.placementRace;

import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.placement.domain.UserPosition;

public interface PlacementRace {
    UserPosition placement();
    UserPoints points();
}
