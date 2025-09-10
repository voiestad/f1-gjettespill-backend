package no.vebb.f1.stats;

import jakarta.persistence.*;

@Entity
@Table(name = "session_types")
public class SessionTypeEntity {
    @Id
    @Column(name = "session_type")
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    protected SessionTypeEntity() {}

    public SessionType sessionType() {
        return sessionType;
    }
}
