package no.vebb.f1.cutoff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceCutoffRepository extends JpaRepository<RaceCutoffEntity, Integer> {
    List<RaceCutoffEntity> findAllByRaceOrderYearOrderByRaceOrderPosition(int year);
}
