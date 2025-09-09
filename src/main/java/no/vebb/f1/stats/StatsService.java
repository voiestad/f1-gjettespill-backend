package no.vebb.f1.stats;

import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.SessionType;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidFlagException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final SessionTypeRepository sessionTypeRepository;
    private final FlagRepository flagRepository;
    private final FlagStatRepository flagStatRepository;

    public StatsService(SessionTypeRepository sessionTypeRepository, FlagRepository flagRepository, FlagStatRepository flagStatRepository) {
        this.sessionTypeRepository = sessionTypeRepository;
        this.flagRepository = flagRepository;
        this.flagStatRepository = flagStatRepository;
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

    public List<Flag> getFlags() {
        return flagRepository.findAll().stream()
                .map(FlagEntity::flagName)
                .map(Flag::new)
                .toList();
    }

    public boolean isValidFlag(String flagName) {
        return flagRepository.existsById(flagName);
    }

    public Year getYearFromFlagId(int id) throws InvalidFlagException {
        return new Year(flagRepository.findYearByFlagId(id).orElseThrow(InvalidFlagException::new));
    }

    public List<RegisteredFlag> getRegisteredFlags(RaceId raceId) {
        return flagStatRepository.findAllByRaceIdOrderBySessionTypeAscRoundAsc(raceId).stream()
                .map(row -> new RegisteredFlag(
                        new Flag(row.flagName()),
                        row.round(),
                        row.flagId(),
                        new SessionType(row.sessionType())
                ))
                .toList();
    }

    public void insertFlagStats(Flag flag, int round, RaceId raceId, SessionType sessionType) {
        flagStatRepository.save(new FlagStatEntity(flag.value, raceId, round, sessionType.value));
    }

    public void deleteFlagStatsById(int flagId) {
        flagStatRepository.deleteById(flagId);
    }
}
