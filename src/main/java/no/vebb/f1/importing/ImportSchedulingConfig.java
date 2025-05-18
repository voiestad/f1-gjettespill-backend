package no.vebb.f1.importing;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private Importer importer;

	@Autowired
	private Database db;

	@Override
	public void configureTasks(@SuppressWarnings("null") ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(Executors.newSingleThreadScheduledExecutor());
		taskRegistrar.addTriggerTask(
			importer::importData,
			new Trigger() {

				@Override
				public Instant nextExecution(@SuppressWarnings("null") TriggerContext context) {
					Instant lastCompletionTime = context.lastCompletion();
					if (lastCompletionTime == null) {
						return Instant.now().plus(Duration.ofMillis(TimeUtil.SECOND * 5));
					}
					return lastCompletionTime.plus(getDelay());
				}
				
			}
		);
	}

	private Duration getDelay() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			RaceId upcomingRaceId = db.getLatestRaceForPlaceGuess(year).id;
			long timeLeft = db.getTimeLeftToGuessRace(upcomingRaceId);
			int timeLeftHours = (int) (timeLeft / 3600);
			boolean isOutsideRaceWeekend = timeLeftHours > 60 || timeLeftHours < -24;
			if (isOutsideRaceWeekend) {
				return Duration.ofMillis(TimeUtil.HOUR * 8);
			}
			boolean isResultImported = upcomingRaceId.equals(db.getLatestRaceId(year));
			if (isResultImported) {
				return Duration.ofMillis(TimeUtil.HALF_HOUR);
			}
		} catch (InvalidYearException e) {
			return Duration.ofMillis(TimeUtil.DAY);
		} catch (EmptyResultDataAccessException e) {
		}
		return Duration.ofMillis(TimeUtil.TEN_MINUTES);
	}
	
}
