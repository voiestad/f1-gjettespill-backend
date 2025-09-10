package no.vebb.f1.placement.collection;

import java.util.UUID;

public interface PlacementGraphResult {
    UUID getUserId();
    String getUsername();
    int getPoints();
    int getPosition();
}
