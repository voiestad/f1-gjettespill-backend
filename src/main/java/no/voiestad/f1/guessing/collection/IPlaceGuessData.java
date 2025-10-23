package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.year.Year;

public interface IPlaceGuessData {
    Category getCategory();
    String getDriver();
    String getRaceName();
    Year getYear();
}
