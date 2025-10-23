package no.voiestad.f1.bingo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BingomasterRepository extends JpaRepository<BingomasterEntity, UUID> {
    List<BingomasterEntity> findAllByOrderByUserUsername();
}
