package no.vebb.f1.competitors.constructor;

import no.vebb.f1.competitors.domain.Constructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructorRepository extends JpaRepository<ConstructorEntity, Constructor> {
}
