package no.voiestad.f1.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(nullable = false, name = "user_id")
    private UUID id;

    @Column(unique = true, nullable = false, name = "username", columnDefinition = "citext")
    private String username;

    UserEntity(UUID id, String username) {
        this.id = id;
        this.username = username;
    }

    protected UserEntity() {
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }
}
