package no.voiestad.f1.race;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.collection.Race;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;

@Service
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceService(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public Optional<Year> getYearFromRaceId(RaceId raceId) {
        return raceRepository.findById(raceId).map(RaceEntity::year);
    }

    public Optional<RaceId> getLatestRaceId(Year year) {
        List<RaceEntity> races = raceRepository.findAllByYearJoinWithRaceResults(year);
        if (races.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(races.get(races.size() - 1).raceId());
    }

    public Optional<RacePosition> getPositionOfRace(RaceId raceId) {
        return raceRepository.findById(raceId).map(RaceEntity::position);
    }

    public Optional<Race> getLatestRaceForPlaceGuess(Year year) {
        List<RaceEntity> races = raceRepository.findAllByYearJoinWithStartingGrid(year);
        if (races.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(races.get(races.size() - 1)).map(Race::fromEntity);
    }

    public List<RaceId> getRaceIdsFinished(Year year) {
        return raceRepository.findAllByYearJoinWithRaceResults(year).stream()
                .map(RaceEntity::raceId)
                .toList();
    }

    public List<RaceEntity> getActiveRaces() {
        return raceRepository.findAllByNotFinished();
    }

    public Optional<RaceId> getLatestStartingGridRaceId(Year year) {
        return getLatestRaceForPlaceGuess(year).map(Race::id);
    }

    public Optional<RaceId> getUpcomingRaceId(Year year) {
        List<RaceEntity> races = raceRepository.findAllByYearNotInRaceResult(year);
        if (races.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(races.get(0).raceId());
    }

    public Optional<RaceId> getLatestStandingsId(Year year) {
        List<RaceEntity> races = raceRepository.findAllByYearJoinWithStandings(year);
        if (races.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(races.get(races.size() - 1).raceId());
    }

    public boolean isRaceAdded(int raceId) {
        return raceRepository.existsById(new RaceId(raceId));
    }

    public void insertRace(int raceId, String raceName, Year year, RacePosition position) {
        RaceEntity newRace = new RaceEntity(new RaceId(raceId), raceName, year, position);
        raceRepository.save(newRace);
    }

    public RacePosition getNewMaxRaceOrderPosition(Year year) {
        return raceRepository.findTopByYearOrderByPositionDesc(year).map(RaceEntity::position)
                .map(RacePosition::next).orElse(new RacePosition());
    }

    public void deleteRace(RaceEntity race) {
        raceRepository.delete(race);
    }

    public List<RaceEntity> raceEntitiesFromSeason(Year year) {
        return raceRepository.findAllByYearOrderByPosition(year);
    }

    public List<Race> getRacesYear(Year year) {
        return mapToRace(raceEntitiesFromSeason(year));
    }

    public List<Race> getRacesYearFinished(Year year) {
        return mapToRace(raceRepository.findAllByYearJoinWithRaceResults(year));
    }

    private List<Race> mapToRace(List<RaceEntity> raceOrderEntities) {
        return raceOrderEntities.stream()
                .map(Race::fromEntity)
                .toList();
    }

    public Optional<Race> getRaceFromId(RaceId raceId) {
        return raceRepository.findById(raceId)
                .map(Race::fromEntity);
    }

    public Optional<RaceEntity> getRaceEntityFromId(int raceId) {
        return raceRepository.findById(new RaceId(raceId));
    }

    public Optional<RaceId> getRaceId(int raceId) {
        return raceRepository.findById(new RaceId(raceId)).map(RaceEntity::raceId);
    }

    public void setRaceOrder(List<RaceEntity> newOrder) {
        raceRepository.saveAll(newOrder);
    }
}
