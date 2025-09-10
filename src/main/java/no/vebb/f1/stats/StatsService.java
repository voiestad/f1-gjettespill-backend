package no.vebb.f1.stats;

import no.vebb.f1.stats.flag.FlagEntity;
import no.vebb.f1.stats.flag.FlagRepository;
import no.vebb.f1.stats.flag.FlagStatEntity;
import no.vebb.f1.stats.flag.FlagStatRepository;
import no.vebb.f1.stats.domain.SessionType;
import no.vebb.f1.stats.sessionTypes.SessionTypeEntity;
import no.vebb.f1.stats.sessionTypes.SessionTypeRepository;
import no.vebb.f1.collection.RegisteredFlag;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<SessionType> getSessionTypes() {
        return sessionTypeRepository.findAll().stream()
                .map(SessionTypeEntity::sessionType)
                .toList();
    }

    public List<Flag> getFlags() {
        return flagRepository.findAll().stream()
                .map(FlagEntity::flagName)
                .toList();
    }

    public Optional<Year> getYearFromFlagId(int id)  {
        return flagRepository.findYearByFlagId(id).map(Year::new);
    }

    public List<RegisteredFlag> getRegisteredFlags(RaceId raceId) {
        return flagStatRepository.findAllByRaceIdOrderBySessionTypeAscRoundAsc(raceId).stream()
                .map(row -> new RegisteredFlag(
                        row.flagName(),
                        row.round(),
                        row.flagId(),
                        row.sessionType()
                ))
                .toList();
    }

    public void insertFlagStats(Flag flag, int round, RaceId raceId, SessionType sessionType) {
        flagStatRepository.save(new FlagStatEntity(flag, raceId, round, sessionType));
    }

    public void deleteFlagStatsById(int flagId) {
        flagStatRepository.deleteById(flagId);
    }
}
