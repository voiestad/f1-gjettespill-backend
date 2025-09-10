package no.vebb.f1.collection;

import no.vebb.f1.mail.notified.IUserNotifiedCount;
import no.vebb.f1.year.Year;

public record UserNotifiedCount(String raceName, int timesNotified, Year year) {
    public static UserNotifiedCount fromIUserNotifiedCount(IUserNotifiedCount iUserNotifiedCount) {
        return new UserNotifiedCount(iUserNotifiedCount.getRaceName(), iUserNotifiedCount.getTimesNotified(),
                new Year(iUserNotifiedCount.getYear()));
    }
}
