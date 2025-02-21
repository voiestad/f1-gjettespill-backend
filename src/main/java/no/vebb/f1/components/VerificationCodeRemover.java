package no.vebb.f1.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;

@Component
public class VerificationCodeRemover {

	@Autowired
	private Database db;

	@Scheduled(fixedRate = 300000, initialDelay = 1000)
	public void removeExpiredCodes() {
		db.removeExpiredVerificationCodes();
	}
}
