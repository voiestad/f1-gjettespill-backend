package no.voiestad.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.CompetitorService;
import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.results.domain.CompetitorPoints;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.results.request.ConstructorStandingsRequest;
import no.voiestad.f1.results.request.DriverStandingsRequest;
import no.voiestad.f1.results.request.RaceResultRequest;
import no.voiestad.f1.results.request.RaceResultRequestBody;
import no.voiestad.f1.race.RaceEntity;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.scoring.ScoreCalculator;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.importing.Importer;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/season/manage")
public class ManageSeasonController {

    private final Importer importer;
    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;
    private final ResultService resultService;
    private final ScoreCalculator scoreCalculator;
    private final CompetitorService competitorService;

    public ManageSeasonController(Importer importer, CutoffService cutoffService, YearService yearService, RaceService raceService, ResultService resultService, ScoreCalculator scoreCalculator, CompetitorService competitorService) {
        this.importer = importer;
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.resultService = resultService;
        this.scoreCalculator = scoreCalculator;
        this.competitorService = competitorService;
    }

    @PostMapping("/reload")
    @Transactional
    public ResponseEntity<?> reloadRace(@RequestParam("id") RaceEntity race) {
        Year year = race.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        importer.importRaceData(race.raceId());
        return new ResponseEntity<>(HttpStatus.OK);
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
        new Thread(importer::importData).start();
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
    public ResponseEntity<?> addRace(@RequestParam("year") Year year, @RequestParam("id") int inputRaceId) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<RaceId> optRaceId = raceService.getRaceId(inputRaceId);
        if (optRaceId.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        importer.importRaceName(inputRaceId, year);
        importer.importData();
        Optional<RaceId> raceId = raceService.getRaceId(inputRaceId);
        if (raceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        cutoffService.setCutoffRace(cutoffService.getDefaultInstant(year), raceId.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/addRaceResult")
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
        if (!addRaceResult(requestBody.raceResult(), raceId, year)
                || !addDriverStandings(requestBody.driverStandings(), raceId, year)
                || !addConstructorStandings(requestBody.constructorStandings(), raceId, year)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        new Thread(scoreCalculator::calculateScores).start();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean addRaceResult(List<RaceResultRequest> raceResult, RaceId raceId, Year year) {
        List<String> positions = raceResult.stream()
                .map(RaceResultRequest::position)
                .toList();
        List<DriverEntity> drivers = raceResult.stream()
                .map(RaceResultRequest::driver)
                .map(competitorService::getDriver)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(driver -> driver.year().equals(year))
                .toList();
        List<CompetitorPoints> points = raceResult.stream()
                .map(RaceResultRequest::points)
                .map(CompetitorPoints::getCompetitorPoints)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<CompetitorPosition> finishingPositions = raceResult.stream()
                .map(RaceResultRequest::finishingPosition)
                .map(CompetitorPosition::getCompetitorPosition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        boolean hasCorrectSizes = drivers.size() == raceResult.size()
                && points.size() == raceResult.size()
                && finishingPositions.size() == raceResult.size();
        boolean noDuplicates = drivers.size() == new HashSet<>(drivers).size();
        if (!hasCorrectSizes || !noDuplicates) {
            return false;
        }
        for (int i = 0; i < raceResult.size(); i++) {
            resultService.insertDriverRaceResult(
                    raceId, positions.get(i), drivers.get(i), points.get(i), finishingPositions.get(i));
        }
        return true;
    }

    private boolean addDriverStandings(List<DriverStandingsRequest> driverStandings, RaceId raceId, Year year) {
        List<DriverEntity> drivers = driverStandings.stream()
                .map(DriverStandingsRequest::driver)
                .map(competitorService::getDriver)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(driver -> driver.year().equals(year))
                .toList();
        List<CompetitorPoints> points = driverStandings.stream()
                .map(DriverStandingsRequest::points)
                .map(CompetitorPoints::getCompetitorPoints)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<CompetitorPosition> positions = driverStandings.stream()
                .map(DriverStandingsRequest::position)
                .map(CompetitorPosition::getCompetitorPosition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        boolean hasCorrectSizes = drivers.size() == driverStandings.size()
                && points.size() == driverStandings.size()
                && positions.size() == driverStandings.size();
        boolean noDuplicates = drivers.size() == new HashSet<>(drivers).size();
        if (!hasCorrectSizes || !noDuplicates) {
            return false;
        }
        for (int i = 0; i < driverStandings.size(); i++) {
            resultService.insertDriverIntoStandings(
                    raceId, drivers.get(i), positions.get(i), points.get(i));
        }
        return true;
    }

    private boolean addConstructorStandings(List<ConstructorStandingsRequest> constructorStandings, RaceId raceId, Year year) {
        List<ConstructorEntity> constructors = constructorStandings.stream()
                .map(ConstructorStandingsRequest::constructor)
                .map(competitorService::getConstructor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(constructor -> constructor.year().equals(year))
                .toList();
        List<CompetitorPoints> points = constructorStandings.stream()
                .map(ConstructorStandingsRequest::points)
                .map(CompetitorPoints::getCompetitorPoints)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<CompetitorPosition> positions = constructorStandings.stream()
                .map(ConstructorStandingsRequest::position)
                .map(CompetitorPosition::getCompetitorPosition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        boolean hasCorrectSizes = constructors.size() == constructorStandings.size()
                && points.size() == constructorStandings.size()
                && positions.size() == constructorStandings.size();
        boolean noDuplicates = constructors.size() == new HashSet<>(constructors).size();
        if (!hasCorrectSizes || !noDuplicates) {
            return false;
        }
        for (int i = 0; i < constructorStandings.size(); i++) {
            resultService.insertConstructorIntoStandings(
                    raceId, constructors.get(i), positions.get(i), points.get(i));
        }
        return true;
    }
}
