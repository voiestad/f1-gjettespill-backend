package no.vebb.f1.controller.open;

import no.vebb.f1.year.YearService;
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

	private final Cutoff cutoff;
	private final Database db;
	private final YearService yearService;

	public RaceGuessController(Cutoff cutoff, Database db, YearService yearService) {
		this.cutoff = cutoff;
		this.db = db;
		this.yearService = yearService;
	}

	@GetMapping
	public ResponseEntity<RaceGuessResponse> guessOverview() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
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
