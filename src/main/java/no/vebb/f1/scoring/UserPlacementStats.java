package no.vebb.f1.scoring;

import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.util.collection.Medals;
import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.year.Year;

import java.util.List;
import java.util.UUID;

public class UserPlacementStats {
    public final List<Placement<Year>> previousPlacements;
    public final Medals medals;
    public final String username;

    public UserPlacementStats(UUID userId, String username, PlacementService placementService) {
        this.previousPlacements = placementService.getPreviousPlacements(userId);
        this.medals = placementService.getMedals(userId);
        this.username = username;
    }

}
