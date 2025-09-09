package no.vebb.f1.controller.admin.season;

import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.year.Year;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season/results")
public class SeasonResultsController {

    private final YearService yearService;
    private final PlacementService placementService;

    public SeasonResultsController(YearService yearService, PlacementService placementService) {
        this.yearService = yearService;
        this.placementService = placementService;
    }

    @Transactional
    @PostMapping("/finalize")
    public ResponseEntity<?> finalizeSeasonResults(@RequestParam("year") int year) {
        Year validYear = yearService.getYear(year);
        placementService.finalizeYear(validYear);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/is-finished/{year}")
    public ResponseEntity<Boolean> isFinalized(@PathVariable("year") int year) {
        Year validYear = yearService.getYear(year);
        return new ResponseEntity<>(yearService.isFinishedYear(validYear), HttpStatus.OK);
    }
}
