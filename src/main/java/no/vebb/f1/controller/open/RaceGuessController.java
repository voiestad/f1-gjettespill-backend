package no.vebb.f1.controller.open;

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

@RestController
@RequestMapping("/api/public/race-guess")
public class RaceGuessController {

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	@GetMapping
	public ResponseEntity<RaceGuessResponse> guessOverview() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			CutoffRace race = db.getLatestRaceForPlaceGuess(year);
			if (cutoff.isAbleToGuessRace(race.id)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			var first = db.getUserGuessesDriverPlace(race.id, new Category("FIRST", db));
			var tenth = db.getUserGuessesDriverPlace(race.id, new Category("TENTH", db));
			String raceName = String.format("%d. %s %d", race.position, race.name, year.value);
			RaceGuessResponse res = new RaceGuessResponse(raceName, first, tenth);

			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException | EmptyResultDataAccessException | NoAvailableRaceException ignored) {
		}
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
}
