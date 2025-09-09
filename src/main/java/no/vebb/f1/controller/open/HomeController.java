package no.vebb.f1.controller.open;

import java.util.List;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.util.response.HomePageResponse;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.graph.Graph;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.year.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class HomeController {

	private final GuessService guessService;
	private final Graph graph;
	private final YearService yearService;

	public HomeController(GuessService guessService, Graph graphCache, YearService yearService) {
		this.guessService = guessService;
		this.graph = graphCache;
		this.yearService = yearService;
	}

	@GetMapping("/api/public/home")
	public ResponseEntity<HomePageResponse> home() {
		List<RankedGuesser> leaderboard = graph.getleaderboard();
		List<String> guessers = null;
		List<GuesserPointsSeason> graph = null;
		try {
			if (leaderboard == null) {
				Year year = yearService.getCurrentYear();
				guessers = guessService.getSeasonGuessers(year).stream()
					.map(UserEntity::username)
					.toList();
			} else {
				graph = this.graph.getGraph();
			}
		} catch (InvalidYearException ignored) {
		}
		HomePageResponse res = new HomePageResponse(graph, leaderboard,  guessers);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
