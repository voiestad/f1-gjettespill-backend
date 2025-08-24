package no.vebb.f1.codes;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.util.TimeUtil;

@Component
public class CodeRemover {

	private final CodeService codeService;

	public CodeRemover(CodeService codeService) {
		this.codeService = codeService;
	}


	@Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
	public void removeExpiredCodes() {
		codeService.removeExpiredVerificationCodes();
		codeService.removeExpiredReferralCodes();
	}
}
