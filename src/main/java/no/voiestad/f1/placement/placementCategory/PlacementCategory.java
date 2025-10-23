package no.voiestad.f1.placement.placementCategory;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.placement.domain.UserPosition;

public interface PlacementCategory {
    Category categoryName();
    UserPosition placement();
    UserPoints points();
}
