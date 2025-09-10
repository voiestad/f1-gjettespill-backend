package no.vebb.f1.placement.collection;

import java.util.UUID;

public interface PlacementLeaderboardResult {
    UUID getUserId();
    String getUsername();
    int getPoints();
    int getPosition();
    int getPlacement();
}
