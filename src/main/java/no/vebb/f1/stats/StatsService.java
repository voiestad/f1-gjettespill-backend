package no.vebb.f1.stats;

import no.vebb.f1.stats.flag.FlagStatEntity;
import no.vebb.f1.stats.flag.FlagStatRepository;
import no.vebb.f1.stats.domain.SessionType;
import no.vebb.f1.collection.RegisteredFlag;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class StatsService {

    private final FlagStatRepository flagStatRepository;

    public StatsService(FlagStatRepository flagStatRepository) {
        this.flagStatRepository = flagStatRepository;
    }

    public List<SessionType> getSessionTypes() {
        return Arrays.asList(SessionType.values());
    }

    public List<Flag> getFlags() {
        return Arrays.asList(Flag.values());
    }

    public Optional<Year> getYearFromFlagId(int id)  {
        return flagStatRepository.findYearByFlagId(id).map(Year::new);
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
