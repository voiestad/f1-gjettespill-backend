package no.voiestad.f1.controller.open;

import java.util.List;

import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.response.UserStatus;
import no.voiestad.f1.collection.Race;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/public")
public class GeneralApiController {

    private final UserService userService;
    private final YearService yearService;
    private final RaceService raceService;
    private final GuessService guessService;

    public GeneralApiController(UserService userService, YearService yearService, RaceService raceService, GuessService guessService) {
        this.userService = userService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.guessService = guessService;
    }

    @GetMapping("/year/list")
    public ResponseEntity<List<Year>> listYears() {
        return new ResponseEntity<>(yearService.getAllYears(), HttpStatus.OK);
    }

    @GetMapping("/race/list/{year}")
    public ResponseEntity<List<Race>> listRaces(
            @PathVariable("year") Year year,
            @RequestParam(required = false, name = "completedOnly", defaultValue = "false") boolean completedOnly) {
        if (completedOnly) {
            return new ResponseEntity<>(raceService.getRacesYearFinished(year), HttpStatus.OK);
        }
        return new ResponseEntity<>(raceService.getRacesYear(year), HttpStatus.OK);
    }

    @GetMapping("/user/status")
    public ResponseEntity<UserStatus> getUserStatus() {
        if (userService.isLoggedIn()) {
            return new ResponseEntity<>(UserStatus.LOGGED_IN, HttpStatus.OK);
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            return new ResponseEntity<>(UserStatus.NO_USERNAME, HttpStatus.OK);
        }
        return new ResponseEntity<>(UserStatus.LOGGED_OUT, HttpStatus.OK);
    }

    @GetMapping("/category/list")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = guessService.getCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
