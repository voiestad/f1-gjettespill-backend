package no.voiestad.f1.controller.open;

import java.util.List;

import no.voiestad.f1.collection.PositionedCompetitorDTO;
import no.voiestad.f1.collection.Race;
import no.voiestad.f1.collection.RegisteredFlag;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.race.RaceEntity;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.stats.StatsService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<RaceStats> raceStats(@PathVariable("raceId") RaceEntity race) {
        return new ResponseEntity<>(
                new RaceStats(race.raceId(), race.year(), resultService, raceService, statsService)
                , HttpStatus.OK);
    }

    public static class RaceStats {

        public final String name;
        public final List<PositionedCompetitorDTO<DriverName>> startingGrid;
        public final List<PositionedCompetitorDTO<DriverName>> raceResult;
        public final List<PositionedCompetitorDTO<DriverName>> driverStandings;
        public final List<PositionedCompetitorDTO<ConstructorName>> constructorStandings;
        public final List<RegisteredFlag> flags;

        public RaceStats(RaceId raceId, Year year, ResultService resultService, RaceService raceService, StatsService statsService) {
            this.startingGrid = resultService.getStartingGrid(raceId).stream().map(PositionedCompetitorDTO::fromStartingGrid).toList();
            this.raceResult = resultService.getRaceResult(raceId).stream().map(PositionedCompetitorDTO::fromRaceResult).toList();
            this.driverStandings = resultService.getDriverStandings(raceId).stream().map(PositionedCompetitorDTO::fromDriverStandings).toList();
            this.constructorStandings = resultService.getConstructorStandings(raceId).stream().map(PositionedCompetitorDTO::fromConstructorStandings).toList();
            this.flags = statsService.getRegisteredFlags(raceId);
            RacePosition position = raceService.getPositionOfRace(raceId).orElse(null);
            String raceName = raceService.getRaceFromId(raceId).map(Race::name).orElse(null);
            this.name = String.format("%s. %s %s", position, raceName, year);
        }
    }
}
