package no.vebb.f1.stats;

import no.vebb.f1.util.domainPrimitive.SessionType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final SessionTypeRepository sessionTypeRepository;

    public StatsService(SessionTypeRepository sessionTypeRepository) {
        this.sessionTypeRepository = sessionTypeRepository;
    }

    public boolean isValidSessionType(String sessionType) {
        return sessionTypeRepository.existsById(sessionType);
    }

    public List<SessionType> getSessionTypes() {
        return sessionTypeRepository.findAll().stream()
                .map(SessionTypeEntity::sessionType)
                .map(SessionType::new)
                .toList();
    }
}
