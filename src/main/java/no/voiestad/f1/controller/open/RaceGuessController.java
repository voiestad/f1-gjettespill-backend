package no.voiestad.f1.controller.open;

import java.util.Optional;

import no.voiestad.f1.collection.Race;
import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.response.RaceGuessResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return raceService.getRaceFromId(raceId).map(this::getGuessOverview)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<RaceGuessResponse> getCurrentGuessOverview() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        Optional<Race> optRace = raceService.getLatestRaceForPlaceGuess(year);
        return optRace.map(this::getGuessOverview)
                .orElse(new ResponseEntity<>(HttpStatus.FORBIDDEN));
    }

    private ResponseEntity<RaceGuessResponse> getGuessOverview(Race race) {
        RaceId raceId = race.id();
        if (cutoffService.isAbleToGuessRace(raceId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        var first = guessService.getUserGuessesDriverPlace(raceId, Category.FIRST);
        var tenth = guessService.getUserGuessesDriverPlace(raceId, Category.TENTH);
        String raceName = String.format("%s. %s %s", race.position(), race.name(), race.year());
        RaceGuessResponse res = new RaceGuessResponse(raceName, first, tenth);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
