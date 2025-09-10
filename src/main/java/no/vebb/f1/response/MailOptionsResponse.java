package no.vebb.f1.response;

import java.util.Map;

public record MailOptionsResponse(boolean hasMail, Map<Integer, Boolean> mailOptions) {
}
