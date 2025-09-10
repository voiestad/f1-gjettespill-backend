package no.vebb.f1.guessing.collection;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

public record PlaceGuess(Category category, Driver driver, String raceName, Year year) {
    public static PlaceGuess fromIPlaceGuess(IPlaceGuess iPlaceGuess) {
        return new PlaceGuess(iPlaceGuess.getCategory(), new Driver(iPlaceGuess.getDriver()),
                iPlaceGuess.getRaceName(), new Year(iPlaceGuess.getYear()));
    }
}
