package no.vebb.f1.placement;

import no.vebb.f1.guessing.category.Category;

public interface PlacementCategory {
    Category categoryName();
    int placement();
    int points();
}
