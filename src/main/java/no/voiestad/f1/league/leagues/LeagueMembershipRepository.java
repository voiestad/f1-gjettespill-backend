package no.voiestad.f1.league.leagues;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueMembershipRepository extends JpaRepository<LeagueMembershipEntity, LeagueMembershipId> {
}
