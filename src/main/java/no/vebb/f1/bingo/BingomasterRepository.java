package no.vebb.f1.bingo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BingomasterRepository extends JpaRepository<BingomasterEntity, UUID> {
    List<BingomasterEntity> findAllByOrderByUserUsername();
}
