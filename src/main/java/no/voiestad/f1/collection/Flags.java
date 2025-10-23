package no.voiestad.f1.collection;

import java.util.Optional;

public class Flags {
    public final int yellow;
    public final int red;
    public final int safetyCar;

    public boolean hasValidValues() {
        return yellow >= 0 && red >= 0 && safetyCar >= 0;
    }

    private Flags(int yellow, int red, int safetyCar) {
        this.yellow = yellow;
        this.red = red;
        this.safetyCar = safetyCar;
    }

    public static Optional<Flags> getFlags(int yellow, int red, int safetyCar) {
        Flags flags = new Flags(yellow, red, safetyCar);
        if (flags.hasValidValues()) {
            return Optional.of(flags);
        }
        return Optional.empty();
    }
}
