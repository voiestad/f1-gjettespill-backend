package no.vebb.f1.controller.open;

import no.vebb.f1.results.ResultService;
import no.vebb.f1.year.YearService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.util.response.HeaderResponse;

@RestController
public class HeaderController {

	private final Database db;
	private final Cutoff cutoff;
	private final UserService userService;
	private final ResultService resultService;
	private final YearService yearService;

	public HeaderController(Database db, Cutoff cutoff, UserService userService, ResultService resultService, YearService yearService) {
		this.db = db;
		this.cutoff = cutoff;
		this.userService = userService;
		this.resultService = resultService;
		this.yearService = yearService;
	}

	@GetMapping("/api/public/header")
	public ResponseEntity<HeaderResponse> preHandle() {
		HeaderResponse res = new HeaderResponse();
		res.isLoggedIn = userService.isLoggedIn();
		res.isRaceGuess = isRaceGuess();
		res.isAdmin =  userService.isAdmin();
		res.isAbleToGuess = cutoff.isAbleToGuessCurrentYear() || isRaceToGuess();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	private boolean isRaceGuess() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			RaceId raceId = db.getLatestRaceForPlaceGuess(year).id;
			return !cutoff.isAbleToGuessRace(raceId);
		} catch (InvalidYearException | NoAvailableRaceException | EmptyResultDataAccessException e) {
			return false;
		}
    }

	private boolean isRaceToGuess() {
		try {
			RaceId raceId = resultService.getCurrentRaceIdToGuess();
			return cutoff.isAbleToGuessRace(raceId);
		} catch (NoAvailableRaceException e) {
			return false;
		}
    }
}
