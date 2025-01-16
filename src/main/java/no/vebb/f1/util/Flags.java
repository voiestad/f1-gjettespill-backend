package no.vebb.f1.util;

public class Flags {
	public int yellow;
	public int red;
	public int safetyCar;

	public boolean hasValidValues() {
		return yellow >= 0 && red >= 0 && safetyCar >= 0;
	}

	public Flags() {
	}

	public Flags(int yellow, int red, int safetyCar) {
		this.yellow = yellow;
		this.red = red;
		this.safetyCar = safetyCar;
	}
}
