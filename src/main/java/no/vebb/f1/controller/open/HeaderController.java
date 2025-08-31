package no.vebb.f1.controller.open;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.util.collection.Race;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.user.UserService;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.util.response.HeaderResponse;

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
        try {
            Year year = new Year(TimeUtil.getCurrentYear(), yearService);
            RaceId raceId = new RaceId(raceService.getLatestRaceForPlaceGuess(year).raceId());
            return !cutoffService.isAbleToGuessRace(raceId);
        } catch (InvalidYearException | NoAvailableRaceException e) {
            return false;
        }
    }

    private boolean isRaceToGuess() {
        try {
            RaceId raceId = resultService.getCurrentRaceIdToGuess();
            return cutoffService.isAbleToGuessRace(raceId);
        } catch (NoAvailableRaceException e) {
            return false;
        }
    }

    private Race ongoingRaceId() {
        try {
            Year year = new Year(TimeUtil.getCurrentYear(), yearService);
            RaceId sgId = raceService.getLatestStartingGridRaceId(year);
            RaceId rrId = raceService.getLatestRaceId(year);
            if (!sgId.equals(rrId)) {
                return raceService.getRaceFromId(sgId);
            }
        } catch (InvalidYearException ignored) {
        }
        return null;
    }
}
