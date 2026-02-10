package no.voiestad.f1.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_providers")
public class UserProviderEntity {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(nullable = false, name = "user_id")
    private UUID userId;
    @Column(nullable = false, name = "provider_id")
    private String providerId;
    @Column(nullable = false, name = "provider")
    private String provider;

    UserProviderEntity(int id, UUID userId, String providerId, String provider) {
        this.id = id;
        this.userId = userId;
        this.providerId = providerId;
        this.provider = provider;
    }

    protected UserProviderEntity() {}

    public UUID userId() {
        return userId;
    }

    public String providerId() {
        return providerId;
    }

    public String provider() {
        return provider;
    }

}
