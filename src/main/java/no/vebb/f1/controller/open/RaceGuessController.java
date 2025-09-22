package no.vebb.f1.controller.open;

import no.vebb.f1.collection.Race;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.year.Year;
import no.vebb.f1.response.RaceGuessResponse;

import java.util.Optional;

@RestController
@RequestMapping("/api/public/race-guess")
public class RaceGuessController {

    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;
    private final GuessService guessService;

    public RaceGuessController(CutoffService cutoffService, YearService yearService, RaceService raceService, GuessService guessService) {
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.guessService = guessService;
    }

    @GetMapping
    public ResponseEntity<RaceGuessResponse> guessOverview(@RequestParam(name = "raceId", required = false) RaceId raceId) {
        if (raceId == null) {
            return getCurrentGuessOverview();
        }
        Optional<Year> optYear = raceService.getYearFromRaceId(raceId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        Optional<Race> optRace = raceService.getRaceFromId(raceId);
        return optRace.map(race -> getGuessOverview(race, year)).orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
    }

    public ResponseEntity<RaceGuessResponse> getCurrentGuessOverview() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        Optional<Race> optRace = raceService.getLatestRaceForPlaceGuess(year);
        return optRace.map(race -> getGuessOverview(race, year)).orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
    }

    private ResponseEntity<RaceGuessResponse> getGuessOverview(Race race, Year year) {
        RaceId raceId = race.id();
        if (cutoffService.isAbleToGuessRace(raceId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        var first = guessService.getUserGuessesDriverPlace(raceId, Category.FIRST);
        var tenth = guessService.getUserGuessesDriverPlace(raceId, Category.TENTH);
        String raceName = String.format("%s. %s %s", race.position(), race.name(), year);
        RaceGuessResponse res = new RaceGuessResponse(raceName, first, tenth);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
