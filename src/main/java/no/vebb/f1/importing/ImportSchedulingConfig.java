package no.vebb.f1.importing;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.year.YearService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class ImportSchedulingConfig implements SchedulingConfigurer {

    private final Importer importer;
    private final YearService yearService;
    private final RaceService raceService;
    private final CutoffService cutoffService;

    public ImportSchedulingConfig(Importer importer, YearService yearService, RaceService raceService, CutoffService cutoffService) {
        this.importer = importer;
        this.yearService = yearService;
        this.raceService = raceService;
        this.cutoffService = cutoffService;
    }

    @Override
    public void configureTasks(@SuppressWarnings("null") ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newSingleThreadScheduledExecutor());
        taskRegistrar.addTriggerTask(importer::importData, new ImportTrigger());
    }

    private Duration getDelay() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return Duration.ofMillis(TimeUtil.DAY);
        }
        Year year = optYear.get();
        Optional<Duration> delay = getDelayCurrentRace(year);
        return delay.orElse(getDelayUpcomingRace(year));
    }

    private Optional<Duration> getDelayCurrentRace(Year year) {
        Optional<RaceId> optCurrentRaceId = raceService.getLatestStartingGridRaceId(year);
        if (optCurrentRaceId.isEmpty()) {
            return Optional.empty();
        }
        RaceId currentRaceId = optCurrentRaceId.get();
        long timeLeft = cutoffService.getTimeLeftToGuessRaceHours(currentRaceId);
        Optional<RaceId> optLatestRaceId = raceService.getLatestRaceId(year);
        if (optLatestRaceId.isEmpty()) {
            return Optional.empty();
        }
        boolean isResultImported = currentRaceId.equals(optLatestRaceId.get());
        boolean isImportPeriod = timeLeft <= 36;
        boolean isRefreshPeriod = timeLeft >= -24;
        if (isImportPeriod && !isResultImported) {
            return Optional.of(Duration.ofMillis(TimeUtil.TEN_MINUTES));
        }
        if (isRefreshPeriod && isResultImported) {
            return Optional.of(Duration.ofMillis(TimeUtil.HALF_HOUR));
        }
        return Optional.empty();
    }

    private Duration getDelayUpcomingRace(Year year) {
        Optional<RaceId> upcomingRaceId = raceService.getUpcomingRaceId(year);
        if (upcomingRaceId.isEmpty()) {
            return Duration.ofMillis(TimeUtil.DAY);
        }
        long timeLeft = cutoffService.getTimeLeftToGuessRaceHours(upcomingRaceId.get());
        boolean shouldImport = timeLeft <= 36;
        if (shouldImport) {
            return Duration.ofMillis(TimeUtil.TEN_MINUTES);
        }
        return Duration.ofMillis(TimeUtil.HOUR * 8);
    }

    private class ImportTrigger implements Trigger {

        @Override
        public Instant nextExecution(TriggerContext context) {
            Instant lastCompletionTime = context.lastCompletion();
            if (lastCompletionTime == null) {
                return Instant.now().plus(Duration.ofMillis(TimeUtil.SECOND * 5));
            }
            return lastCompletionTime.plus(getDelay());
        }


    }
}
