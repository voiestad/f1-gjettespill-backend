package no.vebb.f1.notification.ntfy.message;

import java.util.Collections;
import java.util.List;

public class NtfyMessage {
    private final String topic;
    private final String title;
    private final String message;
    private final List<String> tags;

    NtfyMessage(String topic, String title, String message, List<String> tags) {
        this.topic = topic;
        this.title = title;
        this.message = message;
        this.tags = Collections.unmodifiableList(tags);
    }

    public String getTopic() {
        return topic;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getTags() {
        return tags;
    }
}
