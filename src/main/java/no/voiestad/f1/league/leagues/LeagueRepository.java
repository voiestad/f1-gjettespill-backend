package no.voiestad.f1.league.leagues;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<LeagueEntity, UUID> {
}
