package no.voiestad.f1.guessing.flag;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

@Embeddable
public class FlagGuessId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "flag_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Flag flagName;

    @Embedded
    private Year year;

    protected FlagGuessId() {}

    public FlagGuessId(UUID userId, Flag flagName, Year year) {
        this.userId = userId;
        this.flagName = flagName;
        this.year = year;
    }

    public UUID userId() {
        return userId;
    }

    public Flag flagName() {
        return flagName;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FlagGuessId that)) return false;
        return Objects.equals(year, that.year) && Objects.equals(userId, that.userId) && Objects.equals(flagName, that.flagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, flagName, year);
    }
}
