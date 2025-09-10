package no.vebb.f1.stats.flag;

import jakarta.persistence.*;
import no.vebb.f1.stats.domain.Flag;

@Entity
@Table(name = "flags")
public class FlagEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_name", nullable = false)
    private Flag flagName;

    protected FlagEntity() {}

    public Flag flagName() {
        return flagName;
    }
}
