package no.vebb.f1.guessing.collection;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.year.Year;

public record PlaceGuess(Category category, DriverName driver, String raceName, Year year) {
    public static PlaceGuess fromIPlaceGuess(IPlaceGuess iPlaceGuess) {
        return new PlaceGuess(iPlaceGuess.getCategory(), new DriverName(iPlaceGuess.getDriver()),
                iPlaceGuess.getRaceName(), iPlaceGuess.getYear());
    }
}
