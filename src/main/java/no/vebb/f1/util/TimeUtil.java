package no.vebb.f1.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class TimeUtil {

	public static int getCurrentYear() {
		return Year.now().getValue();
	}

	public static Instant parseTimeInput(String inputTime) throws DateTimeParseException {
		LocalDateTime localDateTime = LocalDateTime.parse(inputTime);
		ZoneId zoneId = ZoneId.of("Europe/Paris");
		ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
		return zonedDateTime.toInstant();
	}

	public static LocalDateTime instantToLocalTime(Instant inputTime) {
		ZoneId zoneId = ZoneId.of("Europe/Paris");
		ZonedDateTime zonedDateTime = inputTime.atZone(zoneId);
		return zonedDateTime.toLocalDateTime();
	}
}
