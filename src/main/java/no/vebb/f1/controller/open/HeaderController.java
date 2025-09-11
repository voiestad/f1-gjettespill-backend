package no.vebb.f1.controller.open;

import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.collection.Race;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.user.UserService;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import no.vebb.f1.response.HeaderResponse;

import java.util.Optional;

@RestController
public class HeaderController {

    private final CutoffService cutoffService;
    private final UserService userService;
    private final ResultService resultService;
    private final YearService yearService;
    private final RaceService raceService;

    public HeaderController(CutoffService cutoffService, UserService userService, ResultService resultService, YearService yearService, RaceService raceService) {
        this.cutoffService = cutoffService;
        this.userService = userService;
        this.resultService = resultService;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    @GetMapping("/api/public/header")
    public ResponseEntity<HeaderResponse> preHandle() {
        HeaderResponse res = new HeaderResponse();
        res.isLoggedIn = userService.isLoggedIn();
        res.isRaceGuess = isRaceGuess();
        res.isAdmin = userService.isAdmin();
        if (res.isAdmin) {
            res.ongoingRace = ongoingRaceId();
        }
        res.isAbleToGuess = cutoffService.isAbleToGuessCurrentYear() || isRaceToGuess();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private boolean isRaceGuess() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return false;
        }
        Year year = optYear.get();
        return raceService.getLatestRaceForPlaceGuess(year).map(RaceOrderEntity::raceId)
                .filter(raceId -> !cutoffService.isAbleToGuessRace(raceId)).isPresent();
    }

    private boolean isRaceToGuess() {
        return resultService.getCurrentRaceIdToGuess().filter(cutoffService::isAbleToGuessRace).isPresent();
    }

    private Race ongoingRaceId() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return null;
        }
        Year year = optYear.get();
        Optional<RaceId> optSgId = raceService.getLatestStartingGridRaceId(year);
        Optional<RaceId> optRrId = raceService.getLatestRaceId(year);
        if (optSgId.isPresent() && optRrId.isPresent()) {
            RaceId sgId = optSgId.get();
            RaceId rrId = optRrId.get();
            if (!sgId.equals(rrId)) {
                return raceService.getRaceFromId(optSgId.get());
            }
        }
        return null;
    }
}
