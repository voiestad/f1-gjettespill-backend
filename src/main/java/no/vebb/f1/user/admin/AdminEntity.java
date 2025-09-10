package no.vebb.f1.user.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "admins")
public class AdminEntity {
    @Id
    @Column(name = "user_id")
    private UUID id;

    public UUID id() {
        return id;
    }
}
