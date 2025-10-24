package no.voiestad.f1.placement.domain;

import no.voiestad.f1.exception.DomainConversionException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserPointsConverter implements Converter<String, UserPoints> {
    @Override
    public UserPoints convert(String source) {
        try {
            return UserPoints.getUserPoints(Integer.parseInt(source)).orElseThrow(DomainConversionException::new);
        } catch (NumberFormatException e) {
            throw new DomainConversionException("Could not parse user points from: " + source);
        }
    }
}
