package no.vebb.f1.notification.notified;

import no.vebb.f1.year.Year;

public interface IUserNotifiedCount {
    String getRaceName();
    int getTimesNotified();
    Year getYear();
}
