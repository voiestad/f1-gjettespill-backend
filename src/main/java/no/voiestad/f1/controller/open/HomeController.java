package no.voiestad.f1.controller.open;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.placement.PlacementService;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.response.HomePageResponse;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.placement.GuesserPointsSeason;
import no.voiestad.f1.collection.RankedGuesser;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final GuessService guessService;
    private final YearService yearService;
    private final PlacementService placementService;
    private final CutoffService cutoffService;

    public HomeController(GuessService guessService, YearService yearService, PlacementService placementService, CutoffService cutoffService) {
        this.guessService = guessService;
        this.yearService = yearService;
        this.placementService = placementService;
        this.cutoffService = cutoffService;
    }

    @GetMapping("/api/public/home")
    public ResponseEntity<HomePageResponse> home(@RequestParam(name = "year", required = false) Integer inputYear) {
        Optional<Year> optYear = inputYear == null ? yearService.getCurrentYear() : yearService.getYear(inputYear);
        List<PublicUserDto> guessers = null;
        List<GuesserPointsSeason> graph = null;
        List<RankedGuesser> leaderboard = null;
        LocalDateTime cutoff = null;
        if (optYear.isPresent()) {
            Year year = optYear.get();
            leaderboard = placementService.getLeaderboard(year);
            if (leaderboard == null) {
                cutoff = cutoffService.getCutoffYearLocalTime(year).orElse(null);
                guessers = guessService.getSeasonGuessers(year).stream()
                        .map(PublicUserDto::fromEntity)
                        .toList();
            } else {
                graph = placementService.getGraph(year);
            }
        }
        HomePageResponse res = new HomePageResponse(graph, leaderboard, guessers, cutoff);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
