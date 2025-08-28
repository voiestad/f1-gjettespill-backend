package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotifiedRepository extends JpaRepository<NotifiedEntity, Integer> {

    int countAllByRaceIdAndUserId(int raceId, UUID userId);
    
    void deleteByUserId(UUID userId);
}
