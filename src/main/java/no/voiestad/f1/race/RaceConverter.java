package no.voiestad.f1.race;

import no.voiestad.f1.exception.DomainConversionException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RaceConverter implements Converter<String, RaceEntity> {
    private final RaceService raceService;

    public RaceConverter(RaceService raceService) {
        this.raceService = raceService;
    }

    @Override
    public RaceEntity convert(String source) {
        try {
            return raceService.getRaceEntityFromId(Integer.parseInt(source)).orElseThrow(DomainConversionException::new);
        } catch (NumberFormatException e) {
            throw new DomainConversionException("Invalid race id: " + source);
        }
    }
}
