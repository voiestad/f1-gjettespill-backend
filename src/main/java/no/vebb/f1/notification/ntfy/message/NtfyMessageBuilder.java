package no.vebb.f1.notification.ntfy.message;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NtfyMessageBuilder {
    private NtfyMessage ntfyMessage = new NtfyMessage(null, null, null, List.of());

    public NtfyMessageBuilder setTopic(String topic) {
        ntfyMessage = new NtfyMessage(topic, ntfyMessage.getTitle(), ntfyMessage.getMessage(), ntfyMessage.getTags());
        return this;
    }

    public NtfyMessageBuilder setTitle(String title) {
        ntfyMessage = new NtfyMessage(ntfyMessage.getTopic(), title, ntfyMessage.getMessage(), ntfyMessage.getTags());
        return this;
    }
    public NtfyMessageBuilder setMessage(String message) {
        ntfyMessage = new NtfyMessage(ntfyMessage.getTopic(), ntfyMessage.getTitle(), message, ntfyMessage.getTags());
        return this;
    }

    public NtfyMessageBuilder setTags(String... tags) {
        ntfyMessage = new NtfyMessage(ntfyMessage.getTopic(), ntfyMessage.getTitle(), ntfyMessage.getMessage(), Arrays.asList(tags));
        return this;
    }

    public Optional<NtfyMessage> build() {
        if (ntfyMessage.getTopic() == null || ntfyMessage.getTopic().isEmpty()
                || ntfyMessage.getTitle() == null || ntfyMessage.getTitle().isEmpty()
                || ntfyMessage.getMessage() == null || ntfyMessage.getMessage().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ntfyMessage);
    }
}
