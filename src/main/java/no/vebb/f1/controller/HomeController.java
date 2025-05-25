package no.vebb.f1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.graph.Graph;
import no.vebb.f1.graph.GraphCache;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
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
		Table leaderboard = graphCache.getleaderboard();
		res.leaderboard = leaderboard;
		try {
			if (leaderboard.getHeader().size() == 0) {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				List<String> guessers = db.getSeasonGuessers(year);
				res.guessers = guessers;
			} else {
				res.graph = graphCache.getGraph();	
			}
		} catch (InvalidYearException e) {
		}
		ResponseEntity<HomePageResponse> entity = new ResponseEntity<>(res, HttpStatus.OK);
		return entity;
	}

	@SuppressWarnings("unused")
	private class HomePageResponse {
		public Graph graph;
		public Table leaderboard;
		public List<String> guessers;
	}

}
