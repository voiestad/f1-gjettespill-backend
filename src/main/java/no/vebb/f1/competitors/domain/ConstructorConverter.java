package no.vebb.f1.competitors.domain;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.exception.DomainConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConstructorConverter implements Converter<String, Constructor> {
    private final CompetitorService competitorService;

    public ConstructorConverter(CompetitorService competitorService) {
        this.competitorService = competitorService;
    }

    @Override
    public Constructor convert(String source) {
        return competitorService.getConstructor(source).orElseThrow(DomainConversionException::new);
    }
}
