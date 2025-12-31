package no.voiestad.f1.cutoff;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.collection.Race;
import no.voiestad.f1.util.TimeUtil;
import no.voiestad.f1.collection.CutoffRace;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;

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

    public Optional<Instant> getCutoffPreRace(RaceId raceId) {
        return raceCutoffRepository.findById(raceId)
                .map(RaceCutoffEntity::cutoff)
                .map(cutoff -> cutoff.minus(Duration.ofDays(3)));
    }

    public Optional<Year> getCurrentYearIfAbleToGuess() {
        return yearService.getCurrentYear().filter(this::isAbleToGuessYear)
                .filter(yearService::isChangableYear);
    }

    public boolean isAbleToGuessYear(Year year) {
        return getCutoffYear(year).filter(this::isAbleToGuess).isPresent();
    }

    public boolean isAbleToGuessRace(RaceId raceId) {
        return getCutoffRace(raceId).filter(this::isAbleToGuess).isPresent();
    }

    public boolean isAbleToGuessPreRace(RaceId raceId) {
        return getCutoffPreRace(raceId).filter(this::isAbleToGuess).isPresent();
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
        return getCutoffRace(raceId).map(this::getTimeLeft).orElse(0L);
    }

    public long getTimeLeftToGuessPreRace(RaceId raceId) {
        return getCutoffPreRace(raceId).map(this::getTimeLeft).orElse(0L);
    }

    public long getTimeLeft(Instant cutoff) {
         return Duration.between(Instant.now(), cutoff).toSeconds();
    }

    public int getTimeLeftToGuessRaceHours(RaceId raceId) {
        return (int) (getTimeLeftToGuessRace(raceId) / 3600L);
    }

    public long getTimeLeftToGuessYear(Year year) {
        return getCutoffYear(year)
                .map(cutoffYear -> Duration.between(Instant.now(), cutoffYear).toSeconds()).orElse(0L);
    }

    public List<CutoffRace> getCutoffRaces(Year year) {
        return raceCutoffRepository.findAllByRaceYearOrderByRacePosition(year).stream()
                .map(row -> new CutoffRace(
                        Race.fromEntity(row.race()),
                        TimeUtil.instantToLocalTime(row.cutoff())))
                .toList();
    }

}
