package no.vebb.f1.scoring;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.Medals;
import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.List;
import java.util.UUID;

public class UserPlacementStats {
    public final List<Placement<Year>> previousPlacements;
    public final Medals medals;

    public UserPlacementStats(Database db, UUID userId) {
        this.previousPlacements = db.getPreviousPlacements(userId);
        this.medals = db.getMedals(userId);
    }

}
