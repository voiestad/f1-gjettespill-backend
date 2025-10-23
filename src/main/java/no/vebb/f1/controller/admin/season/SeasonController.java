package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.vebb.f1.race.RaceEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.importing.Importer;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

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
            @RequestParam("year") int inputYear,
            @RequestParam(name = "start", required = false) Integer start,
            @RequestParam(name = "end", required = false) Integer end) {

        Optional<Year> optYear = yearService.getYear(inputYear);
        if (optYear.isPresent()) {
            String error = String.format("Sesongen %d er allerede lagt til", inputYear);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if (start == null || end == null) {
            Year year = yearService.addYear(inputYear);
            Instant time = cutoffService.getDefaultInstant(year);
            cutoffService.setCutoffYear(time, year);
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        if (start > end) {
            String error = "Starten av året kan ikke være etter slutten av året";
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if (start < 0) {
            String error = "Verdiene kan ikke være negative";
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        Year year = yearService.addYear(inputYear);
        Instant time = cutoffService.getDefaultInstant(year);
        cutoffService.setCutoffYear(time, year);
        List<Integer> races = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            races.add(i);
        }

        importer.importRaceNames(races, year);
        importer.importData();

        setDefaultCutoffRaces(year, time);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private void setDefaultCutoffRaces(Year year, Instant time) {
        List<RaceId> races = raceService.raceEntitiesFromSeason(year).stream().map(RaceEntity::raceId).toList();
        for (RaceId id : races) {
            cutoffService.setCutoffRace(time, id);
        }
    }
}
