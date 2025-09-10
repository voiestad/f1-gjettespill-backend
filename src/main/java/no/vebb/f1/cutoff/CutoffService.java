package no.vebb.f1.cutoff;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.collection.CutoffRace;
import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Service;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidYearException;
import no.vebb.f1.exception.NoAvailableRaceException;

@Service
public class CutoffService {

    private final YearService yearService;
    private final RaceCutoffRepository raceCutoffRepository;
    private final YearCutoffRepository yearCutoffRepository;

    public CutoffService(YearService yearService, RaceCutoffRepository raceCutoffRepository, YearCutoffRepository yearCutoffRepository) {
        this.yearService = yearService;
        this.raceCutoffRepository = raceCutoffRepository;
        this.yearCutoffRepository = yearCutoffRepository;
    }

    public Instant getCutoffYear(Year year) throws InvalidYearException {
        return yearCutoffRepository.findById(year).map(YearCutoffEntity::cutoff).orElseThrow(InvalidYearException::new);
    }

    public Instant getCutoffRace(RaceId raceId) throws NoAvailableRaceException {
        return raceCutoffRepository.findById(raceId).map(RaceCutoffEntity::cutoff).orElseThrow(NoAvailableRaceException::new);
    }

    public boolean isAbleToGuessCurrentYear() {
        try {
            return isAbleToGuessYear(yearService.getCurrentYear());
        } catch (InvalidYearException e) {
            return false;
        }
    }

    public boolean isAbleToGuessYear(Year year) {
        Instant cutoff = getCutoffYear(year);
        return isAbleToGuess(cutoff);
    }

    public boolean isAbleToGuessRace(RaceId raceId) throws NoAvailableRaceException {
        Instant cutoff = getCutoffRace(raceId);
        return isAbleToGuess(cutoff);
    }

    private boolean isAbleToGuess(Instant cutoff) {
        Instant now = Instant.now();
        return cutoff.compareTo(now) > 0;
    }

    public Instant getDefaultInstant(Year year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year.value);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.toInstant();
    }

    public LocalDateTime getCutoffYearLocalTime(Year year) {
        return TimeUtil.instantToLocalTime(getCutoffYear(year));
    }

    public void setCutoffRace(Instant cutoffTime, RaceId raceId) {
        raceCutoffRepository.save(new RaceCutoffEntity(raceId, cutoffTime));
    }

    public void setCutoffYear(Instant cutoffTime, Year year) {
        yearCutoffRepository.save(new YearCutoffEntity(year, cutoffTime));
    }

    public long getTimeLeftToGuessRace(RaceId raceId) throws NoAvailableRaceException {
        Instant now = Instant.now();
        Instant cutoff = getCutoffRace(raceId);
        return Duration.between(now, cutoff).toSeconds();
    }

    public int getTimeLeftToGuessRaceHours(RaceId raceId) throws NoAvailableRaceException {
        return (int) (getTimeLeftToGuessRace(raceId) / 3600L);
    }

    public long getTimeLeftToGuessYear() {
        Instant now = Instant.now();
        Instant cutoffYear = getCutoffYear(yearService.getCurrentYear());
        return Duration.between(now, cutoffYear).toSeconds();
    }

    public List<CutoffRace> getCutoffRaces(Year year) {
        return raceCutoffRepository.findAllByRaceOrderYearOrderByRaceOrderPosition(year).stream()
                .map(row -> new CutoffRace(
                        row.position(),
                        row.raceName(),
                        row.raceId(),
                        TimeUtil.instantToLocalTime(row.cutoff()),
                        year
                ))
                .toList();
    }

}
