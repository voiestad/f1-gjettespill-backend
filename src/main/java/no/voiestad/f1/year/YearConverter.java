package no.voiestad.f1.year;

import no.voiestad.f1.exception.DomainConversionException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class YearConverter implements Converter<String, Year> {

    private final YearService yearService;

    public YearConverter(YearService yearService) {
        this.yearService = yearService;
    }

    @Override
    public Year convert(String source) {
        try {
            int year = Integer.parseInt(source);
            return yearService.getYear(year).orElseThrow(DomainConversionException::new);
        } catch (NumberFormatException e) {
            throw new DomainConversionException("Invalid year: " + source);
        }
    }
}
