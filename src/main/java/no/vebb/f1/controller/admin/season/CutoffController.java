package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.scoring.ScoreCalculator;
import no.vebb.f1.exception.YearFinishedException;
import no.vebb.f1.response.CutoffResponse;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.collection.CutoffRace;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidRaceException;

@RestController
@RequestMapping("/api/admin/season/cutoff")
public class CutoffController {

    private final ScoreCalculator scoreCalculator;
    private final YearService yearService;
    private final RaceService raceService;
    private final CutoffService cutoffService;

    public CutoffController(ScoreCalculator scoreCalculator, YearService yearService, RaceService raceService, CutoffService cutoffService) {
        this.scoreCalculator = scoreCalculator;
        this.yearService = yearService;
        this.raceService = raceService;
        this.cutoffService = cutoffService;
    }

    @GetMapping("/list/{year}")
    public ResponseEntity<CutoffResponse> manageCutoff(@PathVariable("year") Year year) {
        List<CutoffRace> races = cutoffService.getCutoffRaces(year);
        LocalDateTime cutoffYear = cutoffService.getCutoffYearLocalTime(year).orElse(null);
        CutoffResponse res = new CutoffResponse(races, cutoffYear);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/set/race")
    @Transactional
    public ResponseEntity<String> setCutoffRace(
            @RequestParam("id") int raceId,
            @RequestParam("cutoff") String cutoff) {
        try {
            RaceId validRaceId = raceService.getRaceId(raceId);
            Year year = raceService.getYearFromRaceId(validRaceId);
            if (yearService.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
            }
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            cutoffService.setCutoffRace(cutoffTime, validRaceId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException | InvalidRaceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set/year")
    @Transactional
    public ResponseEntity<String> setCutoffYear(
            @RequestParam("year") Year year,
            @RequestParam("cutoff") String cutoff) {
        if (yearService.isFinishedYear(year)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            cutoffService.setCutoffYear(cutoffTime, year);
            new Thread(scoreCalculator::calculateScores).start();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
