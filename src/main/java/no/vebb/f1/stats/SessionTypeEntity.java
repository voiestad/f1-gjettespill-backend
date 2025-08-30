package no.vebb.f1.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_types")
public class SessionTypeEntity {
    @Id
    @Column(name = "session_type")
    private String sessionType;

    protected SessionTypeEntity() {}

    public SessionTypeEntity(String sessionType) {
        this.sessionType = sessionType;
    }

    public String sessionType() {
        return sessionType;
    }
}
