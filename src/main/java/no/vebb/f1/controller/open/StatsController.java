package no.vebb.f1.controller.open;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.stats.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.util.RaceStats;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

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
}
