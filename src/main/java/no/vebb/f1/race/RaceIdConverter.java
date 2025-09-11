package no.vebb.f1.race;

import no.vebb.f1.exception.DomainConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RaceIdConverter implements Converter<String, RaceId> {
    private final RaceService raceService;

    public RaceIdConverter(RaceService raceService) {
        this.raceService = raceService;
    }

    @Override
    public RaceId convert(String source) {
        try {
            return raceService.getRaceId(Integer.parseInt(source)).orElseThrow(DomainConversionException::new);
        } catch (NumberFormatException e) {
            throw new DomainConversionException("Invalid race id: " + source);
        }
    }
}
