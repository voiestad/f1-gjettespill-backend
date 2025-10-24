package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.year.Year;

public record PlaceGuessData(Category category, DriverName driver, String raceName, Year year) {
    public static PlaceGuessData fromIPlaceGuess(IPlaceGuessData iPlaceGuess) {
        return new PlaceGuessData(iPlaceGuess.getCategory(), new DriverName(iPlaceGuess.getDriver()),
                iPlaceGuess.getRaceName(), iPlaceGuess.getYear());
    }
}
