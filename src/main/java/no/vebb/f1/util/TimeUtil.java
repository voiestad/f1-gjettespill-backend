package no.vebb.f1.util;

import java.time.Year;

public class TimeUtil {

	public static int getCurrentYear() {
		return Year.now().getValue();
	}
}
