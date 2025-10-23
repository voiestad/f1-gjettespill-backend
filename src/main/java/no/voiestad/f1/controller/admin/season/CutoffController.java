package no.voiestad.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.collection.CutoffRace;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.response.CutoffResponse;
import no.voiestad.f1.scoring.ScoreCalculator;
import no.voiestad.f1.util.TimeUtil;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season/cutoff")
public class CutoffController {

    private final ScoreCalculator scoreCalculator;
    private final YearService yearService;
    private final RaceService raceService;
    private final CutoffService cutoffService;

    public CutoffController(ScoreCalculator scoreCalculator, YearService yearService, RaceService raceService,
                            CutoffService cutoffService) {
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
            @RequestParam("id") RaceId raceId,
            @RequestParam("cutoff") LocalDateTime cutoffLocal) {
        Optional<Year> optYear = raceService.getYearFromRaceId(raceId).filter(yearService::isChangableYear);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Race is in a year that is over and the cutoff can't be changed.",
                    HttpStatus.FORBIDDEN);
        }
        if (cutoffLocal.getYear() != optYear.get().value) {
            return new ResponseEntity<>("Cutoff can't be outside year of race.", HttpStatus.BAD_REQUEST);
        }
        Instant cutoff = TimeUtil.localTimeToInstant(cutoffLocal);
        cutoffService.setCutoffRace(cutoff, raceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/set/year")
    @Transactional
    public ResponseEntity<String> setCutoffYear(
            @RequestParam("year") Year year,
            @RequestParam("cutoff") LocalDateTime cutoffLocal) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the cutoff for year can't be changed.",
                    HttpStatus.FORBIDDEN);
        }
        if (cutoffLocal.getYear() != year.value) {
            return new ResponseEntity<>("Cutoff can't be outside year.", HttpStatus.BAD_REQUEST);
        }
        Instant cutoff = TimeUtil.localTimeToInstant(cutoffLocal);
        cutoffService.setCutoffYear(cutoff, year);
        new Thread(scoreCalculator::calculateScores).start();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
