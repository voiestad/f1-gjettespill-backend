package no.vebb.f1.controller.open;

import java.util.List;

import no.vebb.f1.user.User;
import no.vebb.f1.util.response.HomePageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.graph.GraphCache;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class HomeController {

	private final Database db;
	private final GraphCache graphCache;

	public HomeController(Database db, GraphCache graphCache) {
		this.db = db;
		this.graphCache = graphCache;
	}

	@GetMapping("/api/public/home")
	public ResponseEntity<HomePageResponse> home() {
		List<RankedGuesser> leaderboard = graphCache.getleaderboard();
		List<String> guessers = null;
		List<GuesserPointsSeason> graph = null;
		try {
			if (leaderboard == null) {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				guessers = db.getSeasonGuessers(year).stream()
					.map(User::username)
					.toList();
			} else {
				graph = graphCache.getGraph();
			}
		} catch (InvalidYearException ignored) {
		}
		HomePageResponse res = new HomePageResponse(graph, leaderboard,  guessers);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
