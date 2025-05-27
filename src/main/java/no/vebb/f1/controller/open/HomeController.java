package no.vebb.f1.controller.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Controller for home and contact page.
 */
@RestController
public class HomeController {

	@Autowired
	private Database db;

	@Autowired
	private GraphCache graphCache;

	/**
	 * Handles GET request for home page.
	 * 
	 * @return home page
	 */
	@GetMapping("/api/public/home")
	public ResponseEntity<HomePageResponse> home() {
		HomePageResponse res = new HomePageResponse();
		List<RankedGuesser> leaderboard = graphCache.getleaderboard();
		res.leaderboard = leaderboard;
		try {
			if (leaderboard == null) {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				List<String> guessers = db.getSeasonGuessers(year).stream()
					.map(user -> user.username)
					.toList();
				res.guessers = guessers;
			} else {
				res.graph = graphCache.getGraph();	
			}
		} catch (InvalidYearException e) {
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	private class HomePageResponse {
		public List<GuesserPointsSeason> graph;
		public List<RankedGuesser> leaderboard;
		public List<String> guessers;
	}

}
