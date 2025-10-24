package no.voiestad.f1.collection;

import no.voiestad.f1.notification.notified.IUserNotifiedCount;
import no.voiestad.f1.year.Year;

public record UserNotifiedCount(String raceName, int timesNotified, Year year) {
    public static UserNotifiedCount fromIUserNotifiedCount(IUserNotifiedCount iUserNotifiedCount) {
        return new UserNotifiedCount(iUserNotifiedCount.getRaceName(), iUserNotifiedCount.getTimesNotified(),
                iUserNotifiedCount.getYear());
    }
}
