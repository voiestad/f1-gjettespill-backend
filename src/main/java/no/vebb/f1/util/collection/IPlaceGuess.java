package no.vebb.f1.util.collection;

import no.vebb.f1.guessing.category.Category;

public interface IPlaceGuess {
    Category getCategory();
    String getDriver();
    String getRaceName();
    int getYear();
}
