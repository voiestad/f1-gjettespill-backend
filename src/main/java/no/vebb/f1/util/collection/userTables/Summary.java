package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.placement.domain.UserPoints;

public record Summary(Placement<UserPoints> drivers,
                      Placement<UserPoints> constructors,
                      Placement<UserPoints> flag,
                      Placement<UserPoints> winner,
                      Placement<UserPoints> tenth,
                      Placement<UserPoints> total) {
}
