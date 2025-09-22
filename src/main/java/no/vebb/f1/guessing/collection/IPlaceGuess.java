package no.vebb.f1.guessing.collection;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.year.Year;

public interface IPlaceGuess {
    Category getCategory();
    String getDriver();
    String getRaceName();
    Year getYear();
}
