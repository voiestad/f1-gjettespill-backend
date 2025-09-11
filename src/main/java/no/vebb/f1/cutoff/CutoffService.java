package no.vebb.f1.cutoff;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.collection.CutoffRace;
import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Service;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

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

    public Optional<Instant> getCutoffYear(Year year) {
        return yearCutoffRepository.findById(year).map(YearCutoffEntity::cutoff);
    }

    public Optional<Instant> getCutoffRace(RaceId raceId) {
        return raceCutoffRepository.findById(raceId).map(RaceCutoffEntity::cutoff);
    }

    public boolean isAbleToGuessCurrentYear() {
        return yearService.getCurrentYear().filter(this::isAbleToGuessYear).isPresent();
    }

    public boolean isAbleToGuessYear(Year year) {
        return getCutoffYear(year).filter(this::isAbleToGuess).isPresent();
    }

    public boolean isAbleToGuessRace(RaceId raceId) {
        return getCutoffRace(raceId).filter(this::isAbleToGuess).isPresent();
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

    public Optional<LocalDateTime> getCutoffYearLocalTime(Year year) {
        return getCutoffYear(year).map(TimeUtil::instantToLocalTime);
    }

    public void setCutoffRace(Instant cutoffTime, RaceId raceId) {
        raceCutoffRepository.save(new RaceCutoffEntity(raceId, cutoffTime));
    }

    public void setCutoffYear(Instant cutoffTime, Year year) {
        yearCutoffRepository.save(new YearCutoffEntity(year, cutoffTime));
    }

    public long getTimeLeftToGuessRace(RaceId raceId) {
        return getCutoffRace(raceId).map(instant -> Duration.between(Instant.now(), instant).toSeconds()).orElse(0L);
    }

    public int getTimeLeftToGuessRaceHours(RaceId raceId) {
        return (int) (getTimeLeftToGuessRace(raceId) / 3600L);
    }

    public long getTimeLeftToGuessYear(Year year) {
        return getCutoffYear(year)
                .map(cutoffYear -> Duration.between(Instant.now(), cutoffYear).toSeconds()).orElse(0L);
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
