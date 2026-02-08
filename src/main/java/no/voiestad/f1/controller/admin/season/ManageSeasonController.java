package no.voiestad.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.CompetitorService;
import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.event.CalculateScoreEvent;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.results.request.RaceResultRequestBody;
import no.voiestad.f1.race.RaceEntity;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.results.request.StartingGridRequestBody;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season/manage")
public class ManageSeasonController {

    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;
    private final ResultService resultService;
    private final CompetitorService competitorService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ManageSeasonController(
            CutoffService cutoffService,
            YearService yearService,
            RaceService raceService,
            ResultService resultService,
            CompetitorService competitorService, ApplicationEventPublisher applicationEventPublisher) {
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.resultService = resultService;
        this.competitorService = competitorService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/move")
    @Transactional
    public ResponseEntity<?> changeRaceOrder(
            @RequestParam("id") RaceEntity raceToMove,
            @RequestParam("newPosition") int inputPosition) {
        Year year = raceToMove.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        RacePosition maxPos = raceService.getNewMaxRaceOrderPosition(year);
        boolean isPosOutOfBounds = inputPosition < 1 || inputPosition > maxPos.toValue() - 1;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<RaceEntity> races = raceService.raceEntitiesFromSeason(year);
        RacePosition currentPos = new RacePosition();
        RacePosition position = RacePosition.getRacePosition(inputPosition).orElseThrow(RuntimeException::new);
        List<RaceEntity> newOrder = new ArrayList<>();
        for (RaceEntity race : races) {
            if (race.raceId().equals(raceToMove.raceId())) {
                continue;
            }
            if (currentPos.equals(position)) {
                newOrder.add(raceToMove.withPosition(currentPos));
                currentPos = currentPos.next();
            }
            newOrder.add(race.withPosition(currentPos));
            currentPos = currentPos.next();
        }
        if (currentPos.equals(position)) {
            newOrder.add(raceToMove.withPosition(currentPos));
        }
        raceService.setRaceOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteRace(@RequestParam("id") RaceEntity raceToDelete) {
        Year year = raceToDelete.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        raceService.deleteRace(raceToDelete);
        List<RaceEntity> races = raceService.raceEntitiesFromSeason(year);
        RacePosition currentPos = new RacePosition();
        List<RaceEntity> newOrder = new ArrayList<>();
        for (RaceEntity race : races) {
            newOrder.add(race.withPosition(currentPos));
            currentPos = currentPos.next();
        }
        raceService.setRaceOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addRace(@RequestParam("year") Year year, @RequestParam("name") String inputRaceName) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (inputRaceName == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String raceName = inputRaceName.strip();
        if (raceName.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RacePosition position = raceService.getNewMaxRaceOrderPosition(year);
        RaceId raceId = raceService.insertRace(raceName, year, position);
        cutoffService.setCutoffRace(cutoffService.getDefaultInstant(year), raceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/starting-grid")
    @Transactional
    public ResponseEntity<?> addStartingGrid(@RequestBody StartingGridRequestBody requestBody) {
        Optional<RaceEntity> optRace = raceService.getRaceEntityFromId(requestBody.raceId());
        if (optRace.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RaceEntity race = optRace.get();
        RaceId raceId = race.raceId();
        Year year = race.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        resultService.clearStartingGridFromRace(raceId);
        List<DriverEntity> drivers = competitorService.getAllDriversWithYear(requestBody.startingGrid(), year);
        if (drivers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CompetitorPosition position = new CompetitorPosition();
        for (DriverEntity driver : drivers) {
            resultService.insertDriverStartingGrid(raceId, driver, position);
            position = position.next();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/race-result")
    @Transactional
    public ResponseEntity<?> addRaceResult(@RequestBody RaceResultRequestBody requestBody) {
        Optional<RaceEntity> optRace = raceService.getRaceEntityFromId(requestBody.raceId());
        if (optRace.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RaceEntity race = optRace.get();
        RaceId raceId = race.raceId();
        Year year = race.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        resultService.clearResultsFromRace(raceId);
        if (!addRaceResult(requestBody.raceResult(), raceId, year)
                || !addDriverStandings(requestBody.driverStandings(), raceId, year)
                || !addConstructorStandings(requestBody.constructorStandings(), raceId, year)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        applicationEventPublisher.publishEvent(new CalculateScoreEvent());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean addRaceResult(List<Integer> raceResult, RaceId raceId, Year year) {
        List<DriverEntity> drivers = competitorService.getAllDriversWithYear(raceResult, year);
        if (drivers.isEmpty()) {
            return false;
        }
        CompetitorPosition position = new CompetitorPosition();
        for (DriverEntity driver : drivers) {
            resultService.insertDriverRaceResult(raceId, driver, position);
            position = position.next();
        }
        return true;
    }

    private boolean addDriverStandings(List<Integer> driverStandings, RaceId raceId, Year year) {
        List<DriverEntity> drivers = competitorService.getAllDriversWithYear(driverStandings, year);
        if (drivers.isEmpty()) {
            return false;
        }
        CompetitorPosition position = new CompetitorPosition();
        for (DriverEntity driver : drivers) {
            resultService.insertDriverIntoStandings(raceId, driver, position);
            position = position.next();
        }
        return true;
    }

    private boolean addConstructorStandings(List<Integer> constructorStandings, RaceId raceId, Year year) {
        List<ConstructorEntity> constructors =
                competitorService.getAllConstructorssWithYear(constructorStandings, year);
        if (constructors.isEmpty()) {
            return false;
        }
        CompetitorPosition position = new CompetitorPosition();
        for (ConstructorEntity constructor : constructors) {
            resultService.insertConstructorIntoStandings(raceId, constructor, position);
            position = position.next();
        }
        return true;
    }
}
