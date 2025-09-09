package no.vebb.f1.competitors;

import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConstructorColorRepository extends JpaRepository<ConstructorColorEntity, ConstructorId> {
    List<ConstructorColorEntity> findAllByIdYearOrderByConstructorYearPosition(Year year);
}
