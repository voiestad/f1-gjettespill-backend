package no.vebb.f1.race;

import jakarta.persistence.EntityManager;
import no.vebb.f1.util.collection.Race;
import no.vebb.f1.year.Year;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidYearException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceOrderRepository raceOrderRepository;
    private final EntityManager entityManager;

    public RaceService(RaceRepository raceRepository, RaceOrderRepository raceOrderRepository, EntityManager entityManager) {
        this.raceRepository = raceRepository;
        this.raceOrderRepository = raceOrderRepository;
        this.entityManager = entityManager;
    }

    public Year getYearFromRaceId(RaceId raceId) throws InvalidRaceException {
        return raceOrderRepository.findById(raceId).map(RaceOrderEntity::year).orElseThrow(InvalidRaceException::new);
    }

    public RaceId getLatestRaceId(Year year) throws InvalidYearException {
        List<RaceOrderEntity> races = raceOrderRepository.findAllByYearJoinWithRaceResults(year);
        if (races.isEmpty()) {
            throw new InvalidYearException("No races completed for " + year);
        }
        return races.get(races.size() - 1).raceId();
    }

    public RacePosition getPositionOfRace(RaceId raceId) throws InvalidRaceException {
        return raceOrderRepository.findById(raceId).map(RaceOrderEntity::position).orElseThrow(InvalidRaceException::new);
    }

    public RaceOrderEntity getLatestRaceForPlaceGuess(Year year) throws InvalidYearException {
        List<RaceOrderEntity> races = raceOrderRepository.findAllByYearJoinWithStartingGrid(year);
        if (races.isEmpty()) {
            throw new InvalidYearException("No races has starting grid for " + year);
        }
        return races.get(races.size() - 1);
    }

    public List<RaceId> getRaceIdsFinished(Year year) {
        return raceOrderRepository.findAllByYearJoinWithRaceResults(year).stream()
                .map(RaceOrderEntity::raceId)
                .toList();
    }

    public List<RaceOrderEntity> getActiveRaces() {
        return raceOrderRepository.findAllByNotFinished();
    }

    public RaceId getLatestStartingGridRaceId(Year year) throws InvalidYearException {
        return getLatestRaceForPlaceGuess(year).raceId();
    }

    public RaceId getUpcomingRaceId(Year year) {
        List<RaceOrderEntity> races = raceOrderRepository.findAllByYearNotInRaceResult(year);
        if (races.isEmpty()) {
            throw new InvalidYearException("Every races are complete for " + year);
        }
        return races.get(0).raceId();
    }

    public RaceId getLatestStandingsId(Year year) throws InvalidYearException {
        List<RaceOrderEntity> races = raceOrderRepository.findAllByYearJoinWithStandings(year);
        if (races.isEmpty()) {
            throw new InvalidYearException("No races has starting grid for " + year);
        }
        return races.get(races.size() - 1).raceId();
    }

    public boolean isRaceAdded(int raceId) {
        return raceRepository.existsById(new RaceId(raceId));
    }

    public void insertRace(int raceId, String raceName) {
        RaceEntity newRace = new RaceEntity(raceId, raceName);
        raceRepository.save(newRace);
    }

    public RacePosition getNewMaxRaceOrderPosition(Year year) {
        return raceOrderRepository.findTopByYearOrderByPositionDesc(year).map(RaceOrderEntity::position)
                .map(RacePosition::next).orElse(new RacePosition());
    }

    public void insertRaceOrder(RaceId raceId, Year year, RacePosition position) {
        RaceOrderEntity raceOrderEntity = new RaceOrderEntity(raceId, year, position);
        raceOrderRepository.save(raceOrderEntity);
    }

    public void updateRaceOrderPosition(RaceId raceId, Year year, RacePosition position) {
        raceOrderRepository.updatePosition(raceId, year, position);
    }

    public void deleteRace(RaceId raceId) {
        raceRepository.deleteById(raceId);
        entityManager.flush();
        entityManager.clear();
    }

    public boolean isRaceInSeason(RaceId raceId, Year year) {
        return raceOrderRepository.existsByRaceIdAndYear(raceId, year);
    }

    public List<RaceId> getRacesFromSeason(Year year) {
        return raceOrderRepository.findAllByYearOrderByPosition(year).stream()
                .map(RaceOrderEntity::raceId)
                .toList();
    }

    public List<Race> getRacesYear(Year year) {
        return mapToRace(raceOrderRepository.findAllByYearOrderByPosition(year));
    }

    public List<Race> getRacesYearFinished(Year year) throws InvalidYearException {
        return mapToRace(raceOrderRepository.findAllByYearJoinWithRaceResults(year));
    }

    private List<Race> mapToRace(List<RaceOrderEntity> raceOrderEntities) {
        return raceOrderEntities.stream()
                .map(ro -> new Race(
                        ro.position(),
                        ro.name(),
                        ro.raceId(),
                        ro.year()
                ))
                .toList();
    }

    public Race getRaceFromId(RaceId raceId) {
        return raceOrderRepository.findById(raceId)
                .map(race -> new Race(race.position(), race.name(), raceId, race.year()))
                .orElseThrow(InvalidRaceException::new);
    }

    public RaceId getRaceId(int raceId) {
        return raceRepository.findById(new RaceId(raceId)).orElseThrow(InvalidRaceException::new).raceId();
    }

}
