package no.voiestad.f1.controller.admin;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.stats.StatsService;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.collection.RegisteredFlag;
import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.stats.domain.SessionType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flag")
public class FlagController {

    private final YearService yearService;
    private final RaceService raceService;
    private final StatsService statsService;

    public FlagController(YearService yearService, RaceService raceService, StatsService statsService) {
        this.yearService = yearService;
        this.raceService = raceService;
        this.statsService = statsService;
    }

    @GetMapping("/types")
    public ResponseEntity<List<Flag>> getFlagTypes() {
        return new ResponseEntity<>(statsService.getFlags(), HttpStatus.OK);
    }

    @GetMapping("/session-types")
    public ResponseEntity<List<SessionType>> getSessionTypes() {
        return new ResponseEntity<>(statsService.getSessionTypes(), HttpStatus.OK);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<List<RegisteredFlag>> getRegisteredFlag(@PathVariable("id") RaceId raceId) {
        List<RegisteredFlag> registeredFlags = statsService.getRegisteredFlags(raceId);
        return new ResponseEntity<>(registeredFlags, HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> registerFlag(
            @RequestParam("flag") Flag flag,
            @RequestParam("round") int round,
            @RequestParam("raceId") RaceId raceId,
            @RequestParam("sessionType") SessionType sessionType) {
        Optional<Year> optYear = raceService.getYearFromRaceId(raceId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the flags can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!isValidRound(round)) {
            return new ResponseEntity<>("Round : '" + round + "' out of bounds. Range: 1-100.",
                    HttpStatus.BAD_REQUEST);
        }
        statsService.insertFlagStats(flag, round, raceId, sessionType);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteFlag(@RequestParam("id") int id) {
        Optional<Year> optYear = statsService.getYearFromFlagId(id);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the flags can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        statsService.deleteFlagStatsById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isValidRound(int round) {
        return round >= 1 && round <= 100;
    }
}
