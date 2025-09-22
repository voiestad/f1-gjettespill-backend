package no.vebb.f1.mail.notified;

import no.vebb.f1.year.Year;

public interface IUserNotifiedCount {
    String getRaceName();
    int getTimesNotified();
    Year getYear();
}
