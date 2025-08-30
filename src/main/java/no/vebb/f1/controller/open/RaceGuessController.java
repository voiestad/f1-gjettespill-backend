package no.vebb.f1.controller.open;

import no.vebb.f1.domain.GuessService;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.year.YearService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.util.response.RaceGuessResponse;

@RestController
@RequestMapping("/api/public/race-guess")
public class RaceGuessController {

	private final CutoffService cutoffService;
	private final Database db;
	private final YearService yearService;
	private final RaceService raceService;
	private final GuessService guessService;

	public RaceGuessController(CutoffService cutoffService, Database db, YearService yearService, RaceService raceService, GuessService guessService) {
		this.cutoffService = cutoffService;
		this.db = db;
		this.yearService = yearService;
		this.raceService = raceService;
		this.guessService = guessService;
	}

	@GetMapping
	public ResponseEntity<RaceGuessResponse> guessOverview() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			RaceOrderEntity race = raceService.getLatestRaceForPlaceGuess(year);
			RaceId raceId = new RaceId(race.raceId());
			if (cutoffService.isAbleToGuessRace(raceId)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			var first = db.getUserGuessesDriverPlace(raceId, new Category("FIRST", guessService));
			var tenth = db.getUserGuessesDriverPlace(raceId, new Category("TENTH", guessService));
			String raceName = String.format("%d. %s %d", race.position(), race.name(), year.value);
			RaceGuessResponse res = new RaceGuessResponse(raceName, first, tenth);

			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException | EmptyResultDataAccessException | NoAvailableRaceException ignored) {
		}
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
}
