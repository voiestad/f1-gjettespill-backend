package no.vebb.f1.controller.admin;

import java.util.List;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.stats.StatsService;
import no.vebb.f1.year.Year;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.util.domainPrimitive.SessionType;
import no.vebb.f1.util.exception.InvalidFlagException;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidSessionTypeException;

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
    public ResponseEntity<List<RegisteredFlag>> getRegisteredFlag(@PathVariable("id") int raceId) {
        try {
            RaceId validRaceId = raceService.getRaceId(raceId);
            List<RegisteredFlag> registeredFlags = statsService.getRegisteredFlags(validRaceId);
            return new ResponseEntity<>(registeredFlags, HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> registerFlag(
            @RequestParam("flag") String flag,
            @RequestParam("round") int round,
            @RequestParam("raceId") int raceId,
            @RequestParam("sessionType") String sessionType) {
        try {
            RaceId validRaceId = raceService.getRaceId(raceId);
            Year year = raceService.getYearFromRaceId(validRaceId);
            if (yearService.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the flags can't be changed");
            }
            Flag validFlag = new Flag(flag, statsService);
            SessionType validSessionType = new SessionType(sessionType, statsService);
            if (!isValidRound(round)) {
                throw new IllegalArgumentException("Round : '" + round + "' out of bounds. Range: 1-100.");
            }
            statsService.insertFlagStats(validFlag, round, validRaceId, validSessionType);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidRaceException | InvalidFlagException | IllegalArgumentException |
                 InvalidSessionTypeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteFlag(@RequestParam("id") int id) {
        try {
            Year year = statsService.getYearFromFlagId(id);
            if (yearService.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the flags can't be changed");
            }
                statsService.deleteFlagStatsById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidFlagException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidRound(int round) {
        return round >= 1 && round <= 100;
    }


}
