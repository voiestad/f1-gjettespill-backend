package no.vebb.f1.scoring.domain;

import no.vebb.f1.exception.DomainConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DiffConverter implements Converter<String, Diff> {

    @Override
    public Diff convert(String source) {
        try {
            return new Diff(Integer.parseInt(source));
        } catch (NumberFormatException e) {
            throw new DomainConversionException("Could not parse diff from " + source);
        }
    }
}
