package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Year;

public record UserNotifiedCount(String raceName, int timesNotified, Year year) {}
