package no.vebb.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.scoring.UserPlacementStats;
import no.vebb.f1.scoring.UserScoreResponse;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserService;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class ProfileController {

    private final UserService userService;
    private final CutoffService cutoffService;
    private final Database db;
    private final YearService yearService;
    private final RaceService raceService;

    public ProfileController(UserService userService, CutoffService cutoffService, Database db, YearService yearService, RaceService raceService) {
        this.userService = userService;
        this.cutoffService = cutoffService;
        this.db = db;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    @GetMapping("/api/public/user/{id}")
    public ResponseEntity<UserScoreResponse> guesserProfile(
            @PathVariable("id") UUID id,
            @RequestParam(value = "raceId", required = false) Integer raceId,
            @RequestParam(value = "year", required = false) Integer year) {
        Optional<UserEntity> optUser = userService.loadUser(id);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserEntity userEntity = optUser.get();
        return getGuesserProfile(userEntity, raceId, year);
    }

    @GetMapping("/api/user/my-profile")
    public ResponseEntity<UserScoreResponse> myProfile(
            @RequestParam(value = "raceId", required = false) Integer raceId,
            @RequestParam(value = "year", required = false) Integer year) {
        Optional<UserEntity> optUser = userService.loadUser();
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserEntity userEntity = optUser.get();
        return getGuesserProfile(userEntity, raceId, year);
    }

    private ResponseEntity<UserScoreResponse> getGuesserProfile(UserEntity userEntity, Integer inputRaceId, Integer inputYear) {
        if (inputRaceId == null) {
            if (inputYear == null) {
                return getUpToDate(userEntity);
            }
            return getGuesserProfileYear(userEntity, inputYear);
        }
        try {
            RaceId raceId = new RaceId(inputRaceId, raceService);
            Year year = raceService.getYearFromRaceId(raceId);
            if (isAbleToSeeGuesses(userEntity, year)) {
                UserScoreResponse res = new UserScoreResponse(PublicUserDto.fromEntity(userEntity), year, raceId, db, raceService);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<UserScoreResponse> getUpToDate(UserEntity userEntity) {
        try {
            Year year = new Year(TimeUtil.getCurrentYear(), yearService);
            if (isAbleToSeeGuesses(userEntity, year)) {
                UserScoreResponse res = new UserScoreResponse(PublicUserDto.fromEntity(userEntity), year, db, raceService);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
        } catch (InvalidYearException ignored) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<UserScoreResponse> getGuesserProfileYear(UserEntity userEntity, int inputYear) {
        try {
            Year year = new Year(inputYear, yearService);
            if (isAbleToSeeGuesses(userEntity, year)) {
                UserScoreResponse res = new UserScoreResponse(PublicUserDto.fromEntity(userEntity), year, db, raceService);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
        } catch (InvalidYearException ignored) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private boolean isAbleToSeeGuesses(UserEntity userEntity, Year year) {
        return !cutoffService.isAbleToGuessYear(year) || userService.isLoggedInUser(userEntity);
    }

    @GetMapping("/api/public/user/list")
    public ResponseEntity<List<PublicUserDto>> listUsers() {
        List<PublicUserDto> res = userService.getAllUsers().stream()
                .map(PublicUserDto::fromEntity)
                .toList();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/public/user/placements/{id}")
    public ResponseEntity<UserPlacementStats> placementStatsById(@PathVariable("id") UUID id) {
        Optional<UserEntity> optUser = userService.loadUser(id);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserPlacementStats res = new UserPlacementStats(db, id, optUser.get().username());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/user/my-placements")
    public ResponseEntity<UserPlacementStats> myPlacementStats() {
        return userService.loadUser().map(this::getPlacementStats).orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    private ResponseEntity<UserPlacementStats> getPlacementStats(UserEntity userEntity) {
        UserPlacementStats res = new UserPlacementStats(db, userEntity.id(), userEntity.username());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
