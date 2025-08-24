package no.vebb.f1.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(nullable = false, name = "user_id")
    private UUID id;
    @Column(unique = true, nullable = false, name = "google_id")
    private String googleId;
    @Column(unique = true, nullable = false, name = "username")
    private String username;

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String googleId() {
        return googleId;
    }

}
