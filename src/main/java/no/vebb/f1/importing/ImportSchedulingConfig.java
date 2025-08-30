package no.vebb.f1.importing;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.year.YearService;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

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
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			Duration delay = getDelayCurrentRace(year);
			if (delay != null) {
				return delay;
			}
			return getDelayUpcomingRace(year);
		} catch (InvalidYearException e) {
			return Duration.ofMillis(TimeUtil.DAY);
		}
	}
	
	private Duration getDelayCurrentRace(Year year) {
		try {
			RaceId currentRaceId = raceService.getLatestStartingGridRaceId(year);
			long timeLeft = cutoffService.getTimeLeftToGuessRaceHours(currentRaceId);
			boolean isResultImported = currentRaceId.equals(raceService.getLatestRaceId(year));
			boolean isImportPeriod = timeLeft <= 36;
			boolean isRefreshPeriod = timeLeft >= -24;
			if (isImportPeriod && !isResultImported) {
				return Duration.ofMillis(TimeUtil.TEN_MINUTES);
			}
			if (isRefreshPeriod && isResultImported) {
				return Duration.ofMillis(TimeUtil.HALF_HOUR);
			}
		} catch (EmptyResultDataAccessException | InvalidYearException | NoAvailableRaceException ignored) {
		}
			return null;
	}

	private Duration getDelayUpcomingRace(Year year) {
		try {
			RaceId upcomingRaceId = raceService.getUpcomingRaceId(year);
			long timeLeft = cutoffService.getTimeLeftToGuessRaceHours(upcomingRaceId);
			boolean shouldImport = timeLeft <= 36;
			if (shouldImport) {
				return Duration.ofMillis(TimeUtil.TEN_MINUTES);
			}
			return Duration.ofMillis(TimeUtil.HOUR * 8);
		} catch (InvalidYearException | NoAvailableRaceException e) {
			return Duration.ofMillis(TimeUtil.DAY); 
		}
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
