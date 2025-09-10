package no.vebb.f1.placement.placementCategory;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.placement.domain.UserPosition;

public interface PlacementCategory {
    Category categoryName();
    UserPosition placement();
    UserPoints points();
}
