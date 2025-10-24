package no.voiestad.f1.codes;

import java.security.SecureRandom;

public class CodeGenerator {

	private static final SecureRandom random = new SecureRandom();
	
	public static long getReferralCode() {
		return random.nextLong(Long.MAX_VALUE - 1000000000000000000L) + 1000000000000000000L;
	}
}
