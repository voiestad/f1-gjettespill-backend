package no.vebb.f1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.util.response.RaceGuessResponse;

/**
 * Class is responsible for displaying guesses for the users for races.
 */
@RestController
@RequestMapping("/api/public/race-guess")
public class RaceGuessController {

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	/**
	 * Handles GET requests for /race-guess. If there is the guesses are not
	 * available, redirects to /. A race is only available when the cutoff of the
	 * latest starting grid of the current year has passed.
	 */
	@GetMapping
	public ResponseEntity<RaceGuessResponse> guessOverview() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			CutoffRace race = db.getLatestRaceForPlaceGuess(year);
			if (cutoff.isAbleToGuessRace(race.id)) {
				return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
			}
			var first = db.getUserGuessesDriverPlace(race.id, new Category("FIRST", db));
			var tenth = db.getUserGuessesDriverPlace(race.id, new Category("TENTH", db));
			String raceName = String.format("%d. %s %d", race.position, race.name, year.value);
			RaceGuessResponse res = new RaceGuessResponse(raceName, first, tenth);

			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException e) {
		} catch (EmptyResultDataAccessException e) {
		} catch (NoAvailableRaceException e) {
		}
		return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
	}
}
