package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import no.vebb.f1.util.response.CutoffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.graph.GraphCache;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;

/**
 * CutoffController is responsible for changing cutoff for races and season.
 */
@RestController
@RequestMapping("/api/admin/season/cutoff")
public class CutoffController {

    @Autowired
    private Database db;

    @Autowired
    private GraphCache graphCache;

    /**
     * Handles GET requests for managing cutoff for the given season. Gives a list
     * of cutoffs for each race of the season and the cutoff for the season. Will
     * redirect to / if user is not admin and /admin/season if year is invalid.
     *
     * @param year of season
     * @return cutoff template
     */
    @GetMapping("/list/{year}")
    public ResponseEntity<CutoffResponse> manageCutoff(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, db);
        List<CutoffRace> races = db.getCutoffRaces(seasonYear);
        LocalDateTime cutoffYear = db.getCutoffYearLocalTime(seasonYear);
        CutoffResponse res = new CutoffResponse(races, cutoffYear);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Handels POST mapping for setting the cutoff for a race. Will
     * redirect to / if user is not admin and /admin/season if year is invalid. If
     * race ID is or time has invalid format, nothing will change in the database.
     *
     * @param raceId of race
     * @param cutoff to set for the race in local time
     * @return redirect to origin if user is admin and season valid
     */
    @PostMapping("/set/race")
    @Transactional
    public ResponseEntity<String> setCutoffRace(
            @RequestParam("id") int raceId,
            @RequestParam("cutoff") String cutoff) {
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            db.setCutoffRace(cutoffTime, validRaceId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException | InvalidRaceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handels POST mapping for setting the cutoff for a season. Will
     * redirect to / if user is not admin and /admin/season if year is invalid. If
     * time has invalid format, nothing will change in the database.
     *
     * @param year   of season
     * @param cutoff to set for the race in local time
     * @return redirect to origin if user is admin and season valid
     */
    @PostMapping("/set/year")
    @Transactional
    public ResponseEntity<String> setCutoffYear(
            @RequestParam("year") int year,
            @RequestParam("cutoff") String cutoff) {
        Year seasonYear = new Year(year, db);
        try {
            Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
            db.setCutoffYear(cutoffTime, seasonYear);
            graphCache.refresh();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
