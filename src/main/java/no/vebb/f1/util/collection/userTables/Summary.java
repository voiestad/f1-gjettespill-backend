package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.util.domainPrimitive.Points;

public record Summary(Placement<Points> drivers,
                      Placement<Points> constructors,
                      Placement<Points> flag,
                      Placement<Points> winner,
                      Placement<Points> tenth,
                      Placement<Points> total) {
}
