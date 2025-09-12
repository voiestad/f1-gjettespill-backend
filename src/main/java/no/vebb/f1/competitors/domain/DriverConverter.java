package no.vebb.f1.competitors.domain;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.exception.DomainConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DriverConverter implements Converter<String, Driver> {
    private final CompetitorService competitorService;

    public DriverConverter(CompetitorService competitorService) {
        this.competitorService = competitorService;
    }

    @Override
    public Driver convert(String source) {
        return competitorService.getDriver(source).orElseThrow(DomainConversionException::new);
    }
}
