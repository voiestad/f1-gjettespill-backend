package no.vebb.f1.response;

import no.vebb.f1.mail.mailOption.MailOption;

import java.util.Map;

public record MailOptionsResponse(boolean hasMail, Map<MailOption, Boolean> mailOptions) {
}
