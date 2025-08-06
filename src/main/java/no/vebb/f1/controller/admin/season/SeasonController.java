package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.importing.Importer;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
@RequestMapping("/api/admin/season")
public class SeasonController {

    private final Database db;
    private final Importer importer;
    private final Cutoff cutoff;

    public SeasonController(Database db, Importer importer, Cutoff cutoff) {
        this.db = db;
        this.importer = importer;
        this.cutoff = cutoff;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addSeason(
            @RequestParam("year") int year,
            @RequestParam(name = "start", required = false) Integer start,
            @RequestParam(name = "end", required = false) Integer end) {
        try {
            new Year(year, db);
            String error = String.format("Sesongen %d er allerede lagt til", year);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (InvalidYearException ignored) {
        }
        db.addYear(year);
        Year seasonYear = new Year(year, db);
        Instant time = cutoff.getDefaultInstant(seasonYear);
        db.setCutoffYear(time, seasonYear);
        if (start == null || end == null) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        if (start > end) {
            String error = "Starten av året kan ikke være etter slutten av året";
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if (start < 0 || year < 0) {
            String error = "Verdiene kan ikke være negative";
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        List<Integer> races = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            races.add(i);
        }

        importer.importRaceNames(races, year);
        importer.importData();

        setDefaultCutoffRaces(seasonYear, time);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private void setDefaultCutoffRaces(Year year, Instant time) {
        List<RaceId> races = db.getRacesFromSeason(year);
        for (RaceId id : races) {
            db.setCutoffRace(time, id);
        }
    }
}
