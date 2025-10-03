package no.vebb.f1.notification.ntfy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ntfy_topics")
public class NtfyTopicEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "topic", nullable = false, unique = true)
    private UUID topic;

    protected NtfyTopicEntity() {}

    public NtfyTopicEntity(UUID userId, UUID topic) {
        this.userId = userId;
        this.topic = topic;
    }

    public UUID userId() {
        return userId;
    }

    public UUID topic() {
        return topic;
    }
}
