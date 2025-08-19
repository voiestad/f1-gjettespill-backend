package no.vebb.f1.controller.admin.season;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season/results")
public class SeasonResultsController {

    private final Database db;

    public SeasonResultsController(Database db) {
        this.db = db;
    }

    @Transactional
    @PostMapping("/finalize")
    public ResponseEntity<?> finalizeSeasonResults(@RequestParam("year") int year) {
        Year validYear = new Year(year, db);
        db.finalizeYear(validYear);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/is-finished/{year}")
    public ResponseEntity<Boolean> isFinalized(@PathVariable("year") int year) {
        Year validYear = new Year(year, db);
        return new ResponseEntity<>(db.isFinishedYear(validYear), HttpStatus.OK);
    }
}
