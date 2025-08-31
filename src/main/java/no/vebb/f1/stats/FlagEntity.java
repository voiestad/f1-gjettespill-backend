package no.vebb.f1.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "flags")
public class FlagEntity {
    @Id
    @Column(name = "flag_name")
    private String flagName;

    protected FlagEntity() {}

    public FlagEntity(String flagName) {
        this.flagName = flagName;
    }

    public String flagName() {
        return flagName;
    }
}
