package no.vebb.f1.controller.open;

import no.vebb.f1.collection.PositionedCompetitor;
import no.vebb.f1.collection.Race;
import no.vebb.f1.collection.RegisteredFlag;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.stats.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/stats")
public class StatsController {

    private final ResultService resultService;
    private final RaceService raceService;
    private final StatsService statsService;

    public StatsController(ResultService resultService, RaceService raceService, StatsService statsService) {
        this.resultService = resultService;
        this.raceService = raceService;
        this.statsService = statsService;
    }

    @GetMapping("/race/{raceId}")
    public ResponseEntity<RaceStats> raceStats(@PathVariable("raceId") RaceId raceId) {
        Optional<Year> year = raceService.getYearFromRaceId(raceId);
        if (year.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        RaceStats res = new RaceStats(raceId, year.get(), resultService, raceService, statsService);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    public static class RaceStats {

        public final String name;
        public final List<PositionedCompetitor<Driver>> startingGrid;
        public final List<PositionedCompetitor<Driver>> raceResult;
        public final List<PositionedCompetitor<Driver>> driverStandings;
        public final List<PositionedCompetitor<Constructor>> constructorStandings;
        public final List<RegisteredFlag> flags;

        public RaceStats(RaceId raceId, Year year, ResultService resultService, RaceService raceService, StatsService statsService) {
            this.startingGrid = resultService.getStartingGrid(raceId).stream().map(PositionedCompetitor::fromStartingGrid).toList();
            this.raceResult = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
            this.driverStandings = resultService.getDriverStandings(raceId).stream().map(PositionedCompetitor::fromDriverStandings).toList();
            this.constructorStandings = resultService.getConstructorStandings(raceId).stream().map(PositionedCompetitor::fromConstructorStandings).toList();
            this.flags = statsService.getRegisteredFlags(raceId);
            RacePosition position = raceService.getPositionOfRace(raceId).orElse(null);
            String raceName = raceService.getRaceFromId(raceId).map(Race::name).orElse(null);
            this.name = String.format("%s. %s %s", position, raceName, year);
        }
    }
}
