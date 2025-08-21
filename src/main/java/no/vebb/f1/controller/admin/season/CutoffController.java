package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import no.vebb.f1.scoring.ScoreCalculator;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.util.response.CutoffResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;

@RestController
@RequestMapping("/api/admin/season/cutoff")
public class CutoffController {

    private final Database db;
    private final ScoreCalculator scoreCalculator;

    public CutoffController(Database db, ScoreCalculator scoreCalculator) {
        this.db = db;
        this.scoreCalculator = scoreCalculator;
    }

    @GetMapping("/list/{year}")
    public ResponseEntity<CutoffResponse> manageCutoff(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, db);
        List<CutoffRace> races = db.getCutoffRaces(seasonYear);
        LocalDateTime cutoffYear = db.getCutoffYearLocalTime(seasonYear);
        CutoffResponse res = new CutoffResponse(races, cutoffYear);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/set/race")
    @Transactional
    public ResponseEntity<String> setCutoffRace(
            @RequestParam("id") int raceId,
            @RequestParam("cutoff") String cutoff) {
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            Year year = db.getYearFromRaceId(validRaceId);
            if (db.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
            }
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            db.setCutoffRace(cutoffTime, validRaceId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException | InvalidRaceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set/year")
    @Transactional
    public ResponseEntity<String> setCutoffYear(
            @RequestParam("year") int year,
            @RequestParam("cutoff") String cutoff) {
        Year validYear = new Year(year, db);
        if (db.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            db.setCutoffYear(cutoffTime, validYear);
            scoreCalculator.calculateScores();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
