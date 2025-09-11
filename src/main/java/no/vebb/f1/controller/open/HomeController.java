package no.vebb.f1.controller.open;

import java.util.List;
import java.util.Optional;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.response.HomePageResponse;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.placement.GuesserPointsSeason;
import no.vebb.f1.collection.RankedGuesser;
import no.vebb.f1.year.Year;

@RestController
public class HomeController {

    private final GuessService guessService;
    private final YearService yearService;
    private final PlacementService placementService;

    public HomeController(GuessService guessService, YearService yearService, PlacementService placementService) {
        this.guessService = guessService;
        this.yearService = yearService;
        this.placementService = placementService;
    }

    @GetMapping("/api/public/home")
    public ResponseEntity<HomePageResponse> home() {
        Optional<Year> optYear = yearService.getCurrentYear();
        List<String> guessers = null;
        List<GuesserPointsSeason> graph = null;
        List<RankedGuesser> leaderboard = null;
        if (optYear.isPresent()) {
            Year year = optYear.get();
            leaderboard = placementService.getLeaderboard(year);
            if (leaderboard == null) {
                guessers = guessService.getSeasonGuessers(year).stream()
                        .map(UserEntity::username)
                        .toList();
            } else {
                graph = placementService.getGraph(year);
            }
        }
        HomePageResponse res = new HomePageResponse(graph, leaderboard, guessers);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
