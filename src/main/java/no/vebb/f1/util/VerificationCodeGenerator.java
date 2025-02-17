package no.vebb.f1.util;

import java.security.SecureRandom;

public class VerificationCodeGenerator {
	
	private VerificationCodeGenerator(){}

	private static SecureRandom random = new SecureRandom();

	public static int getCode() {
		return random.nextInt(900000000) + 100000000;
	}
}
