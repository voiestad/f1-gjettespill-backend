package no.voiestad.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.placement.PlacementService;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.scoring.ScoreService;
import no.voiestad.f1.scoring.UserPlacementStats;
import no.voiestad.f1.scoring.UserScoreResponse;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final UserService userService;
    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;
    private final PlacementService placementService;
    private final GuessService guessService;
    private final ScoreService scoreService;
    private final ResultService resultService;

    public ProfileController(
            UserService userService,
            CutoffService cutoffService,
            YearService yearService,
            RaceService raceService,
            PlacementService placementService,
            GuessService guessService,
            ScoreService scoreService,
            ResultService resultService) {
        this.userService = userService;
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.placementService = placementService;
        this.guessService = guessService;
        this.scoreService = scoreService;
        this.resultService = resultService;
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
        UserEntity userEntity = userService.getUser();
        return getGuesserProfile(userEntity, raceId, year);
    }

    private ResponseEntity<UserScoreResponse> getGuesserProfile(
            UserEntity userEntity, Integer inputRaceId, Integer inputYear) {
        if (inputRaceId == null) {
            if (inputYear == null) {
                return getUpToDate(userEntity);
            }
            Optional<Year> optYear = yearService.getYear(inputYear);
            return getFromYear(userEntity, optYear);
        }
        Optional<RaceId> optRaceId = raceService.getRaceId(inputRaceId);
        if (optRaceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RaceId raceId = optRaceId.get();
        Optional<Year> optYear = raceService.getYearFromRaceId(raceId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Year year = optYear.get();
        if (notAvailableToUser(userEntity, year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        UserScoreResponse res = new UserScoreResponse(PublicUserDto.fromEntity(userEntity),
                year, raceId, raceService, placementService, guessService, scoreService, resultService);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private ResponseEntity<UserScoreResponse> getUpToDate(UserEntity userEntity) {
        Optional<Year> optYear = yearService.getCurrentYear();
        return getFromYear(userEntity, optYear);
    }

    private ResponseEntity<UserScoreResponse> getFromYear(
            UserEntity userEntity, Optional<Year> optYear) {
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Year year = optYear.get();
        if (notAvailableToUser(userEntity, year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        UserScoreResponse res = new UserScoreResponse(
                PublicUserDto.fromEntity(userEntity), year, raceService,
                placementService, guessService, scoreService, resultService);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private boolean notAvailableToUser(UserEntity userEntity, Year year) {
        return cutoffService.isAbleToGuessYear(year) && !userService.isLoggedInUser(userEntity);
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
        UserPlacementStats res = placementService.getPlacementsStats(id, optUser.get().username());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/user/my-placements")
    public ResponseEntity<UserPlacementStats> myPlacementStats() {
        return getPlacementStats(userService.getUser());
    }

    private ResponseEntity<UserPlacementStats> getPlacementStats(UserEntity userEntity) {
        UserPlacementStats res = placementService.getPlacementsStats(userEntity.id(), userEntity.username());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
