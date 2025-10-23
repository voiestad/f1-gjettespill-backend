package no.voiestad.f1.scoring.userTables;

import no.voiestad.f1.collection.Placement;
import no.voiestad.f1.placement.domain.UserPoints;

public record Summary(Placement<UserPoints> drivers,
                      Placement<UserPoints> constructors,
                      Placement<UserPoints> flag,
                      Placement<UserPoints> winner,
                      Placement<UserPoints> tenth,
                      Placement<UserPoints> total) {
}
