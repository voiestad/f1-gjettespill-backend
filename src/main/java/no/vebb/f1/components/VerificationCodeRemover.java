package no.vebb.f1.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;

@Component
public class VerificationCodeRemover {

	@Autowired
	private Database db;

	@Scheduled(fixedRate = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
	public void removeExpiredCodes() {
		db.removeExpiredVerificationCodes();
	}
}
