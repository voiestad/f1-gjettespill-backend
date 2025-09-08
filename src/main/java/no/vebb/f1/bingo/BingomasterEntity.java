package no.vebb.f1.bingo;

import jakarta.persistence.*;
import no.vebb.f1.user.UserEntity;

import java.util.UUID;

@Entity
@Table(name = "bingomasters")
public class BingomasterEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    protected BingomasterEntity() {}

    public BingomasterEntity(UUID userId) {
        this.userId = userId;
    }

    public UserEntity user() {
        return user;
    }
}
