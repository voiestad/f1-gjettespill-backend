package no.vebb.f1.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(nullable = false, name = "user_id")
    private UUID id;
    @Column(unique = true, nullable = false, name = "google_id")
    private String googleId;
    @Column(unique = true, nullable = false, name = "username", columnDefinition = "citext")
    private String username;

    UserEntity(UUID id, String googleId, String username) {
        this.id = id;
        this.googleId = googleId;
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

    public String googleId() {
        return googleId;
    }

    void setUsername(String username) {
        this.username = username;
    }
}
