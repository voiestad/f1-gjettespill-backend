package no.voiestad.f1.notification.notified;

import no.voiestad.f1.year.Year;

public interface IUserNotifiedCount {
    String getRaceName();
    int getTimesNotified();
    Year getYear();
}
