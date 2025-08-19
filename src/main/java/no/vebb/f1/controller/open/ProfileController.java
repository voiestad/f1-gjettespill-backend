package no.vebb.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.scoring.UserPlacementStats;
import no.vebb.f1.scoring.UserScoreResponse;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.exception.InvalidRaceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class ProfileController {

    private final UserService userService;
    private final Cutoff cutoff;
    private final Database db;

    public ProfileController(UserService userService, Cutoff cutoff, Database db) {
        this.userService = userService;
        this.cutoff = cutoff;
        this.db = db;
    }

    @GetMapping("/api/public/user/{id}")
    public ResponseEntity<UserScoreResponse> guesserProfile(
            @PathVariable("id") UUID id,
            @RequestParam(value = "raceId", required = false) Integer raceId) {
        Optional<User> optUser = userService.loadUser(id);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = optUser.get();
        return getGuesserProfile(user, raceId);
    }

    @GetMapping("/api/user/my-profile")
    public ResponseEntity<UserScoreResponse> myProfile(
            @RequestParam(value = "raceId", required = false) Integer raceId) {
        Optional<User> optUser = userService.loadUser();
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = optUser.get();
        return getGuesserProfile(user, raceId);
    }

    private ResponseEntity<UserScoreResponse> getGuesserProfile(User user, Integer inputRaceId) {
        if (inputRaceId == null) {
            return getUpToDate(user);
        }
        try {
            RaceId raceId = new RaceId(inputRaceId, db);
            Year year = db.getYearFromRaceId(raceId);
            UserScoreResponse res = new UserScoreResponse(new PublicUser(user), year, raceId, db);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<UserScoreResponse> getUpToDate(User user) {
        try {
            Year year = new Year(TimeUtil.getCurrentYear(), db);
            if (isAbleToSeeGuesses(user, year)) {
                UserScoreResponse res = new UserScoreResponse(new PublicUser(user), year, db);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
        } catch (InvalidYearException ignored) {
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private boolean isAbleToSeeGuesses(User user, Year year) {
        return !cutoff.isAbleToGuessYear(year) || userService.isLoggedInUser(user);
    }

    @GetMapping("/api/public/user/list")
    public ResponseEntity<List<PublicUser>> listUsers() {
        List<PublicUser> res = db.getAllUsers().stream()
                .map(PublicUser::new)
                .toList();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/public/user/placements/{id}")
    public ResponseEntity<UserPlacementStats> placementStatsById(@PathVariable("id") UUID id) {
        if (userService.loadUser(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserPlacementStats res = new UserPlacementStats(db, id);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/user/my-placements")
    public ResponseEntity<UserPlacementStats> myPlacementStats() {
        return userService.loadUser().map(user ->
                getPlacementStats(user.id())).orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    private ResponseEntity<UserPlacementStats> getPlacementStats(UUID id) {
        UserPlacementStats res = new UserPlacementStats(db, id);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
