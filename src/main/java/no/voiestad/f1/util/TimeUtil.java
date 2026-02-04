package no.voiestad.f1.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class TimeUtil {

	public static final long SECOND = 1000;
	public static final long MINUTE = SECOND * 60;
	public static final long HALF_MINUTE = MINUTE / 2;
	public static final long FIVE_MINUTES = MINUTE * 5;
	public static final long TEN_MINUTES = MINUTE * 10;
	public static final long HOUR = MINUTE * 60;
	public static final long HALF_HOUR = HOUR / 2;
	public static final long DAY = HOUR * 24;

	private static final ZoneId TIMEZONE = ZoneId.of("Europe/Paris");

	public static int getCurrentYear() {
		return Year.now(TIMEZONE).getValue();
	}

	public static Instant localTimeToInstant(LocalDateTime inputTime) throws DateTimeParseException {
		ZonedDateTime zonedDateTime = inputTime.atZone(TIMEZONE);
		return zonedDateTime.toInstant();
	}

	public static LocalDateTime instantToLocalTime(Instant inputTime) {
		ZonedDateTime zonedDateTime = inputTime.atZone(TIMEZONE);
		return zonedDateTime.toLocalDateTime();
	}
}
