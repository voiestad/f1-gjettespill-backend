package no.voiestad.f1.controller.admin.season;

import java.time.Instant;
import java.util.Optional;

import no.voiestad.f1.year.YearService;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season")
public class SeasonController {

    private final CutoffService cutoffService;
    private final YearService yearService;

    public SeasonController(CutoffService cutoffService, YearService yearService) {
        this.cutoffService = cutoffService;
        this.yearService = yearService;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addSeason(@RequestParam("year") int inputYear) {
        Optional<Year> optYear = yearService.getYear(inputYear);
        if (optYear.isPresent()) {
            String error = String.format("Sesongen %d er allerede lagt til", inputYear);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        Year year = yearService.addYear(inputYear);
        Instant time = cutoffService.getDefaultInstant(year);
        cutoffService.setCutoffYear(time, year);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
