package no.vebb.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.exception.InvalidRaceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
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
    public ResponseEntity<UserScore> guesserProfile(
            @PathVariable("id") UUID id,
            @RequestParam(value = "raceId", required = false) Integer raceId) {
        Optional<User> optUser = userService.loadUser(id);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = optUser.get();
        return getGuesserProfile(user, raceId);
    }

    @GetMapping("/api/user/myprofile")
    public ResponseEntity<UserScore> myProfile(
            @RequestParam(value = "raceId", required = false) Integer raceId) {
        Optional<User> optUser = userService.loadUser();
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = optUser.get();
        return getGuesserProfile(user, raceId);
    }

    private ResponseEntity<UserScore> getGuesserProfile(User user, Integer inputRaceId) {
        if (inputRaceId == null) {
            return getUpToDate(user);
        }
        try {
            RaceId raceId = new RaceId(inputRaceId, db);
            Year year = db.getYearFromRaceId(raceId);
            UserScore res = new UserScore(new PublicUser(user), year, raceId, db);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<UserScore> getUpToDate(User user) {
        try {
            Year year = new Year(TimeUtil.getCurrentYear(), db);
            if (isAbleToSeeGuesses(user, year)) {
                UserScore res = new UserScore(new PublicUser(user), year, db);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
        } catch (InvalidYearException e) {
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private boolean isAbleToSeeGuesses(User user, Year year) {
        return !cutoff.isAbleToGuessYear(year) || userService.isLoggedInUser(user);
    }

    @GetMapping("/api/public/user/list")
    public ResponseEntity<List<PublicUser>> listUsers() {
        List<PublicUser> res = db.getAllUsers().stream()
                .map(user -> new PublicUser(user))
                .toList();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
