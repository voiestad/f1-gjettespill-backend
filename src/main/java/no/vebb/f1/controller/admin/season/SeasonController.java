package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.importing.Importer;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
@RequestMapping("/api/admin/season")
public class SeasonController {

    private final Importer importer;
    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;

    public SeasonController(Importer importer, CutoffService cutoffService, YearService yearService, RaceService raceService) {
        this.importer = importer;
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addSeason(
            @RequestParam("year") int year,
            @RequestParam(name = "start", required = false) Integer start,
            @RequestParam(name = "end", required = false) Integer end) {
        try {
            yearService.getYear(year);
            String error = String.format("Sesongen %d er allerede lagt til", year);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (InvalidYearException ignored) {
        }
        yearService.addYear(year);
        Year seasonYear = yearService.getYear(year);
        Instant time = cutoffService.getDefaultInstant(seasonYear);
        cutoffService.setCutoffYear(time, seasonYear);
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

        importer.importRaceNames(races, seasonYear);
        importer.importData();

        setDefaultCutoffRaces(seasonYear, time);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private void setDefaultCutoffRaces(Year year, Instant time) {
        List<RaceId> races = raceService.getRacesFromSeason(year);
        for (RaceId id : races) {
            cutoffService.setCutoffRace(time, id);
        }
    }
}
