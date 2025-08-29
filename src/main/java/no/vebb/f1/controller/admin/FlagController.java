package no.vebb.f1.controller.admin;

import java.util.List;

import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.SessionType;
import no.vebb.f1.util.exception.InvalidFlagException;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidSessionTypeException;

@RestController
@RequestMapping("/api/admin/flag")
public class FlagController {

    private final Database db;
    private final YearService yearService;

    public FlagController(Database db, YearService yearService) {
        this.db = db;
        this.yearService = yearService;
    }

    @GetMapping("/types")
    public ResponseEntity<List<Flag>> getFlagTypes() {
        return new ResponseEntity<>(db.getFlags(), HttpStatus.OK);
    }

    @GetMapping("/session-types")
    public ResponseEntity<List<SessionType>> getSessionTypes() {
        return new ResponseEntity<>(db.getSessionTypes(), HttpStatus.OK);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<List<RegisteredFlag>> getRegisteredFlag(@PathVariable("id") int raceId) {
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            List<RegisteredFlag> registeredFlags = db.getRegisteredFlags(validRaceId);
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
            RaceId validRaceId = new RaceId(raceId, db);
            Year year = db.getYearFromRaceId(validRaceId);
            if (yearService.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the flags can't be changed");
            }
            Flag validFlag = new Flag(flag, db);
            SessionType validSessionType = new SessionType(sessionType, db);
            if (!isValidRound(round)) {
                throw new IllegalArgumentException("Round : '" + round + "' out of bounds. Range: 1-100.");
            }
            db.insertFlagStats(validFlag, round, validRaceId, validSessionType);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidRaceException | InvalidFlagException | IllegalArgumentException |
                 InvalidSessionTypeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteFlag(@RequestParam("id") int id) {
        Year year = db.getYearFromFlagId(id);
        if (yearService.isFinishedYear(year)) {
            throw new YearFinishedException("Year '" + year + "' is over and the flags can't be changed");
        }
        db.deleteFlagStatsById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isValidRound(int round) {
        return round >= 1 && round <= 100;
    }


}
