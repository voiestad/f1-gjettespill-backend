package no.vebb.f1.importing;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Configuration
public class ImportSchedulingConfig implements SchedulingConfigurer {

	private final Importer importer;
	private final Database db;

	public ImportSchedulingConfig(Importer importer, Database db) {
		this.importer = importer;
		this.db = db;
	}

	@Override
	public void configureTasks(@SuppressWarnings("null") ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(Executors.newSingleThreadScheduledExecutor());
		taskRegistrar.addTriggerTask(importer::importData, new ImportTrigger());
	}

	private Duration getDelay() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
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
			RaceId currentRaceId = db.getLatestStartingGridRaceId(year);
			long timeLeft = db.getTimeLeftToGuessRaceHours(currentRaceId);
			boolean isResultImported = currentRaceId.equals(db.getLatestRaceId(year));
			boolean isImportPeriod = timeLeft <= 36;
			boolean isRefreshPeriod = timeLeft >= -24;
			if (isImportPeriod && !isResultImported) {
				return Duration.ofMillis(TimeUtil.TEN_MINUTES);
			}
			if (isRefreshPeriod && isResultImported) {
				return Duration.ofMillis(TimeUtil.HALF_HOUR);
			}
		} catch (EmptyResultDataAccessException ignored) {
		}
		return null;
	}

	private Duration getDelayUpcomingRace(Year year) {
		try {
			RaceId upcomingRaceId = db.getUpcomingRaceId(year);
			long timeLeft = db.getTimeLeftToGuessRaceHours(upcomingRaceId);
			boolean shouldImport = timeLeft <= 36;
			if (shouldImport) {
				return Duration.ofMillis(TimeUtil.TEN_MINUTES);
			}
			return Duration.ofMillis(TimeUtil.HOUR * 8);
		} catch (EmptyResultDataAccessException e) {
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
