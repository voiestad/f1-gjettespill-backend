package no.voiestad.f1.competitors.domain;

import no.voiestad.f1.competitors.CompetitorService;
import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.exception.DomainConversionException;
import no.voiestad.f1.exception.NoUsernameException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConstructorConverter implements Converter<String, ConstructorEntity> {
    private final CompetitorService competitorService;

    public ConstructorConverter(CompetitorService competitorService) {
        this.competitorService = competitorService;
    }

    @Override
    public ConstructorEntity convert(String source) {
        try {
            return competitorService.getConstructor(Integer.parseInt(source)).orElseThrow(DomainConversionException::new);
        } catch (NoUsernameException e) {
            throw new DomainConversionException();
        }
    }
}
