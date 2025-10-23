package no.voiestad.f1.competitors.domain;

import no.voiestad.f1.competitors.CompetitorService;
import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.exception.DomainConversionException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DriverConverter implements Converter<String, DriverEntity> {
    private final CompetitorService competitorService;

    public DriverConverter(CompetitorService competitorService) {
        this.competitorService = competitorService;
    }

    @Override
    public DriverEntity convert(String source) {
        try {
            return competitorService.getDriver(Integer.parseInt(source)).orElseThrow(DomainConversionException::new);
        } catch (NumberFormatException e) {
            throw new DomainConversionException();
        }
    }
}
