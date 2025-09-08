package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Year;

public record PlaceGuess(Category category, Driver driver, String raceName, Year year) {
    public static PlaceGuess fromIPlaceGuess(IPlaceGuess iPlaceGuess) {
        return new PlaceGuess(new Category(iPlaceGuess.getCategory()), new Driver(iPlaceGuess.getDriver()),
                iPlaceGuess.getRaceName(), new Year(iPlaceGuess.getYear()));
    }
}
