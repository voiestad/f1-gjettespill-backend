package no.voiestad.f1.stats;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.stats.flag.FlagStatEntity;
import no.voiestad.f1.stats.flag.FlagStatRepository;
import no.voiestad.f1.stats.domain.SessionType;
import no.voiestad.f1.collection.RegisteredFlag;
import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;

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
        return flagStatRepository.findYearByFlagId(id);
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
