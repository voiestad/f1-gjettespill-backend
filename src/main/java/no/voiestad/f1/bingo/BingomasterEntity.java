package no.voiestad.f1.bingo;

import java.util.UUID;

import no.voiestad.f1.user.UserEntity;

import jakarta.persistence.*;

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
