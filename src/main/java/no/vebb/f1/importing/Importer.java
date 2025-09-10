package no.vebb.f1.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.io.StringWriter;
import java.io.PrintWriter;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.mail.MailService;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.results.domain.CompetitorPosition;
import no.vebb.f1.scoring.ScoreCalculator;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.year.YearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import no.vebb.f1.collection.PositionedCompetitor;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidYearException;

@Component
public class Importer {

    private static final Logger logger = LoggerFactory.getLogger(Importer.class);

    private final MailService mailService;
    private final ScoreCalculator scoreCalculator;
    private final ResultService resultService;
    private final YearService yearService;
    private final RaceService raceService;
    private final CompetitorService competitorService;

    public Importer(MailService mailService, ScoreCalculator scoreCalculator, ResultService resultService, YearService yearService, RaceService raceService, CompetitorService competitorService) {
        this.mailService = mailService;
        this.scoreCalculator = scoreCalculator;
        this.resultService = resultService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.competitorService = competitorService;
    }

    @Transactional
    public void importData() {
        logger.info("Starting import of data to database");
        try {
            Year year = yearService.getCurrentYear();
            Map<Year, List<RaceId>> racesToImportFromList = getActiveRaces();
            boolean shouldImportStandings = false;

            for (Entry<Year, List<RaceId>> racesToImportFrom : racesToImportFromList.entrySet()) {
                Year raceYear = racesToImportFrom.getKey();
                List<RaceId> races = racesToImportFrom.getValue();
                logger.info("Year '{}' has '{}' races to import", raceYear, races.size());
                int startingGridCount = importStartingGrids(races);
                logger.info("Imported '{}' starting grid", startingGridCount);
                boolean hasAddedNewRaceResult = importRaceResults(races);
                if (year.equals(raceYear)) {
                    if (hasAddedNewRaceResult) {
                        logger.info("New race result imported, will import standings");
                        shouldImportStandings = true;
                    } else if (!areStandingsUpToDate(year)) {
                        logger.info("Standings not up to date, will import standings");
                        shouldImportStandings = true;
                    } else {
                        logger.info("Standings and race results are up to date");
                    }
                }
            }
            ResultChangeStatus changeStatus = refreshLatestImports(year);
            if (!changeStatus.equals(ResultChangeStatus.NO_CHANGE)) {
                logger.info("Changes to the race result, will import standings");
                shouldImportStandings = true;
            }
            if (shouldImportStandings) {
                boolean standingsNew = importStandings(year, changeStatus.getPointsChange());
                if (!standingsNew) {
                    if (!changeStatus.equals(ResultChangeStatus.OUTSIDE_POINTS_CHANGE)) {
                        logger.info("Standings were not new.");
                        throw new RuntimeException("Standings were not up to date with race result.");
                    } else {
                        scoreCalculator.calculateScores();
                        logger.info("Race result changed outside points without standings changing. Sending message to admins.");
                        mailService.sendServerMessageToAdmins(
                                "Endringer i resultat av løp utenfor poengene uten at mesterskapet endret seg. Vennligst verifiser at mesterskapet er korrekt.");
                    }
                } else {
                    scoreCalculator.calculateScores();
                    logger.info("Imported standings");
                }
            }
            logger.info("Finished import of data to database");
        } catch (InvalidYearException e) {
            logger.error("Could not import data to the database because current year is not valid");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String errorMessage = stringWriter.toString();
            logger.error("Exception while importing. Rolling back.\n{}", errorMessage);
        }
    }

    private boolean areStandingsUpToDate(Year year) {
        try {
            RaceId latestRaceId = raceService.getLatestRaceId(year);
            try {
                RaceId standingsRaceId = raceService.getLatestStandingsId(year);
                return latestRaceId.equals(standingsRaceId);
            } catch (InvalidYearException e) {
                return false;
            }
        } catch (InvalidYearException e) {
            return true;
        }
    }

    private Map<Year, List<RaceId>> getActiveRaces() {
        Map<Year, List<RaceId>> activeRaces = new LinkedHashMap<>();
        List<RaceOrderEntity> sqlRes = raceService.getActiveRaces();
        for (RaceOrderEntity race : sqlRes) {
            Year year = race.year();
            if (!activeRaces.containsKey(year)) {
                activeRaces.put(year, new ArrayList<>());
            }
            List<RaceId> races = activeRaces.get(year);
            races.add(race.raceId());
        }
        return activeRaces;
    }

    public void importRaceData(RaceId raceId) {
        logger.info("Race data from '{}' manually reloaded by admin", raceId);
        importStartingGridData(raceId);
        ResultChangeStatus changeStatus = importRaceResultData(raceId);
        if (!changeStatus.equals(ResultChangeStatus.NO_CHANGE)) {
            logger.info("Changes to the race result");
        } else {
            logger.info("No change in race result");
        }
        try {
            Year year = yearService.getCurrentYear();
            RaceId newestRaceId = raceService.getLatestRaceId(year);
            if (raceId.equals(newestRaceId)) {
                logger.info("Race that was manually reloaded is the newest race. Will import standings as well");
                importStandings(year, new CompetitorPoints());
            }
        } catch (InvalidYearException | EmptyResultDataAccessException ignored) {
        }
        scoreCalculator.calculateScores();
    }

    private void importStartingGridData(RaceId raceId) {
        List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
        if (startingGrid.size() <= 1) {
            return;
        }
        insertStartingGridData(raceId, startingGrid);
    }

    private ResultChangeStatus importRaceResultData(RaceId raceId) {
        List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
        if (raceResult.size() <= 1) {
            return ResultChangeStatus.NO_CHANGE;
        }
        List<PositionedCompetitor<Driver>> preList = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
        insertRaceResultData(raceId, raceResult);
        List<PositionedCompetitor<Driver>> postList = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
        if (preList.size() != postList.size()) {
            logger.info("Different size");
            ResultChangeStatus status = ResultChangeStatus.POINTS_CHANGE;
            CompetitorPoints change = compPoints(postList);
            status.setPointsChange(change);
            return status;
        }
        for (int i = 0; i < preList.size(); i++) {
            PositionedCompetitor<Driver> pre = preList.get(i);
            PositionedCompetitor<Driver> post = postList.get(i);
            if (!pre.equals(post)) {
                if (i < 10) {
                    return ResultChangeStatus.POINTS_CHANGE;
                }
                return ResultChangeStatus.OUTSIDE_POINTS_CHANGE;
            }
        }
        return ResultChangeStatus.NO_CHANGE;
    }

    private ResultChangeStatus refreshLatestImports(Year year) {
        refreshLatestStartingGrid(year);
        return refreshLatestRaceResult(year);
    }

    private void refreshLatestStartingGrid(Year year) {
        try {
            RaceId raceId = raceService.getLatestStartingGridRaceId(year);
            importStartingGridData(raceId);
        } catch (InvalidYearException ignored) {
        }
    }

    private ResultChangeStatus refreshLatestRaceResult(Year year) {
        try {
            RaceId raceId = raceService.getLatestRaceId(year);
            return importRaceResultData(raceId);
        } catch (InvalidYearException e) {
            return ResultChangeStatus.NO_CHANGE;
        }
    }

    private int importStartingGrids(List<RaceId> racesToImportFrom) {
        int count = 0;
        for (RaceId raceId : racesToImportFrom) {
            boolean isAlreadyAdded = resultService.isStartingGridAdded(raceId);
            if (isAlreadyAdded) {
                continue;
            }
            List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
            if (startingGrid.size() <= 1) {
                break;
            }
            insertStartingGridData(raceId, startingGrid);
            count++;
        }
        return count;
    }

    private void insertStartingGridData(RaceId raceId, List<List<String>> startingGrid) {
        try {
            for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
                CompetitorPosition position = new CompetitorPosition(Integer.parseInt(row.get(0)));
                Driver driver = getDriver(row.get(2), raceId);
                resultService.insertDriverStartingGrid(raceId, position, driver);
            }

        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new RuntimeException("Failed parsing starting grid for race " + raceId + ".\n" + getTableString(startingGrid));
        }
    }

    private boolean importRaceResults(List<RaceId> racesToImportFrom) {
        boolean addedNewRace = false;
        for (RaceId raceId : racesToImportFrom) {
            boolean isAlreadyAdded = resultService.isRaceResultAdded(raceId);
            if (isAlreadyAdded) {
                throw new RuntimeException("Race is already added and was attempted added again");
            }
            List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
            if (raceResult.size() <= 1) {
                break;
            }
            insertRaceResultData(raceId, raceResult);
            addedNewRace = true;
        }
        return addedNewRace;
    }

    private void insertRaceResultData(RaceId raceId, List<List<String>> raceResult) {
        try {
            CompetitorPosition finishingPosition = new CompetitorPosition();
            List<List<String>> disqualified = new ArrayList<>();
            for (List<String> row : raceResult.subList(1, raceResult.size())) {
                String position = row.get(0);
                if (position.equals("DQ")) {
                    disqualified.add(row);
                    continue;
                }
                insertRaceResultRow(raceId, row, finishingPosition);
                finishingPosition = finishingPosition.next();
            }
            for (List<String> row : disqualified) {
                insertRaceResultRow(raceId, row, finishingPosition);
                finishingPosition = finishingPosition.next();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new RuntimeException("Failed parsing race result for race " + raceId + ".\n" + getTableString(raceResult));
        }
    }

    private void insertRaceResultRow(RaceId raceId, List<String> row, CompetitorPosition finishingPosition) {
        String position = row.get(0);
        Driver driver = getDriver(row.get(2), raceId);
        CompetitorPoints points = new CompetitorPoints((int) Double.parseDouble(row.get(6)));
        resultService.insertDriverRaceResult(raceId, position, driver, points, finishingPosition);
    }

    public void importRaceNames(List<Integer> racesToImportFrom, Year year) {
        RacePosition position = new RacePosition();
        for (int raceId : racesToImportFrom) {
            if (addRace(raceId, year, position)) {
                position = position.next();
            }
        }
    }

    public void importRaceName(int raceId, Year year) {
        RacePosition position = raceService.getNewMaxRaceOrderPosition(year);
        addRace(raceId, year, position);
    }

    private boolean addRace(int raceId, Year year, RacePosition position) {
        boolean isAlreadyAdded = raceService.isRaceAdded(raceId);
        if (isAlreadyAdded) {
            throw new RuntimeException("Race name was already added");
        }
        String raceName = TableImporter.getGrandPrixName(raceId);
        if (raceName.isEmpty()) {
            return false;
        }
        raceService.insertRace(raceId, raceName);
        RaceId validRaceId = raceService.getRaceId(raceId);
        raceService.insertRaceOrder(validRaceId, year, position);
        return true;
    }

    private boolean importStandings(Year year, CompetitorPoints expectedChange) {
        try {
            RaceId newestRace = raceService.getLatestRaceId(year);
            ResultChangeStatus driverStatus = importDriverStandings(year, newestRace);
            ResultChangeStatus constructorStatus = importConstructorStandings(year, newestRace);
            boolean equalPointsChange = driverStatus.getPointsChange().equals(constructorStatus.getPointsChange());
            boolean driverValidStatus = validResultStatus(driverStatus, expectedChange);
            boolean constructorValidStatus = validResultStatus(constructorStatus, expectedChange);
            return equalPointsChange && driverValidStatus && constructorValidStatus;
        } catch (InvalidYearException e) {
            throw new RuntimeException("Should not call importStandings without having a race result");
        }
    }

    private boolean validResultStatus(ResultChangeStatus status, CompetitorPoints expectedChange) {
        boolean hasChanged = status == ResultChangeStatus.POINTS_CHANGE;
        boolean greatEnoughChange = status.getPointsChange().compareTo(expectedChange) >= 0;
        return hasChanged && greatEnoughChange;
    }

    private ResultChangeStatus importDriverStandings(Year year, RaceId newestRace) {
        List<List<String>> standings = TableImporter.getDriverStandings(year);
        if (standings.size() <= 1) {
            logger.info("Driver standings not available");
            return ResultChangeStatus.NO_CHANGE;
        }
        standings = standings.subList(1, standings.size());
        try {
            List<PositionedCompetitor<Driver>> currentStandings = standings.stream()
                    .map(row ->
                            new PositionedCompetitor<>(
                                    String.valueOf(Integer.parseInt(row.get(0))),
                                    getDriver(row.get(1), year),
                                    new CompetitorPoints(Integer.parseInt(row.get(4)))
                            ))
                    .toList();
            ResultChangeStatus status = isDriverStandingsNew(currentStandings, year);
            if (status != ResultChangeStatus.POINTS_CHANGE) {
                logger.info("Driver standings are not new, will not add new");
                return ResultChangeStatus.NO_CHANGE;
            }
            for (PositionedCompetitor<Driver> competitor : currentStandings) {
                Driver driver = competitorService.getDriver(competitor.name().toString());
                CompetitorPosition position = new CompetitorPosition(Integer.parseInt(competitor.position()));
                CompetitorPoints points = competitor.points();
                resultService.insertDriverIntoStandings(newestRace, driver, position, points);
            }
            logger.info("Driver standings added for race '{}'", newestRace);
            return status;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new RuntimeException("Failed parsing driver standings.\n" + getTableString(standings));
        }
    }

    private ResultChangeStatus isDriverStandingsNew(List<PositionedCompetitor<Driver>> standings, Year year) {
        try {
            RaceId previousRaceId = raceService.getLatestStandingsId(year);
            List<PositionedCompetitor<Driver>> previousStandings = resultService.getDriverStandings(previousRaceId).stream().map(PositionedCompetitor::fromDriverStandings).toList();
            return compareStandings(standings, previousStandings);
        } catch (InvalidYearException e) {
            return changeWithSum(standings);
        }
    }

    private ResultChangeStatus importConstructorStandings(Year year, RaceId newestRace) {
        List<List<String>> standings = TableImporter.getConstructorStandings(year);
        if (standings.size() <= 1) {
            logger.info("Constructor standings not available");
            return ResultChangeStatus.NO_CHANGE;
        }
        standings = standings.subList(1, standings.size());
        try {
            List<PositionedCompetitor<Constructor>> currentStandings = standings.stream()
                    .map(row ->
                            new PositionedCompetitor<>(
                                    String.valueOf(Integer.parseInt(row.get(0))),
                                    competitorService.getConstructor(row.get(1)),
                                    new CompetitorPoints(Integer.parseInt(row.get(2)))
                            ))
                    .toList();
            ResultChangeStatus status = isConstructorStandingsNew(currentStandings, year);
            if (status != ResultChangeStatus.POINTS_CHANGE) {
                logger.info("Constructor standings are not new, will not add new");
                return ResultChangeStatus.NO_CHANGE;
            }
            for (PositionedCompetitor<Constructor> competitor : currentStandings) {
                CompetitorPosition position = new CompetitorPosition(Integer.parseInt(competitor.position()));
                CompetitorPoints points = competitor.points();
                Constructor validConstructor = competitor.name();
                resultService.insertConstructorIntoStandings(newestRace, validConstructor, position, points);
            }
            logger.info("Constructor standings added for race '{}'", newestRace);
            return status;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new RuntimeException("Failed parsing constructor standings.\n" + getTableString(standings));
        }
    }

    private ResultChangeStatus isConstructorStandingsNew(List<PositionedCompetitor<Constructor>> standings, Year year) {
        try {
            RaceId previousRaceId = raceService.getLatestStandingsId(year);
            List<PositionedCompetitor<Constructor>> previousStandings = resultService.getConstructorStandings(previousRaceId).stream().map(PositionedCompetitor::fromConstructorStandings).toList();
            return compareStandings(standings, previousStandings);
        } catch (InvalidYearException e) {
            return changeWithSum(standings);
        }
    }

    private <T extends Competitor> ResultChangeStatus compareStandings(List<PositionedCompetitor<T>> standings, List<PositionedCompetitor<T>> previousStandings) {
        ResultChangeStatus status = null;
        if (standings.size() != previousStandings.size()) {
            status = ResultChangeStatus.POINTS_CHANGE;
        } else {
            for (int i = 0; i < standings.size(); i++) {
                PositionedCompetitor<T> previousCompetitor = previousStandings.get(i);
                PositionedCompetitor<T> competitor = standings.get(i);
                if (!previousCompetitor.equals(competitor)) {
                    status = ResultChangeStatus.POINTS_CHANGE;
                    break;
                }
            }
            if (status == null) {
                status = ResultChangeStatus.NO_CHANGE;
            }
        }
        Diff diff = new Diff(compPoints(standings).value - compPoints(previousStandings).value);
        status.setPointsChange(new CompetitorPoints(diff.toValue()));
        return status;
    }

    private <T extends Competitor> ResultChangeStatus changeWithSum(List<PositionedCompetitor<T>> competitors) {
        ResultChangeStatus status = ResultChangeStatus.POINTS_CHANGE;
        CompetitorPoints change = compPoints(competitors);
        status.setPointsChange(change);
        return status;
    }

    private <T extends Competitor> CompetitorPoints compPoints(List<PositionedCompetitor<T>> competitors) {
        return competitors.stream()
                .map(PositionedCompetitor::points)
                .reduce(new CompetitorPoints(), CompetitorPoints::add);
    }

    private String parseDriverName(String driverName) {
        return driverName.substring(0, driverName.length() - 3);
    }

    private Driver getDriver(String driverName, RaceId raceId) {
        return competitorService.getAlternativeDriverName(parseDriverName(driverName), raceId);
    }

    private Driver getDriver(String driverName, Year year) {
        return competitorService.getAlternativeDriverName(parseDriverName(driverName), year);
    }

    private String getTableString(List<List<String>> table) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < table.size(); i++) {
            buffer.append("Row: ").append(i + 1).append('\n').append(table.get(i)).append('\n');
        }
        return buffer.toString();
    }

    private enum ResultChangeStatus {
        NO_CHANGE,
        POINTS_CHANGE,
        OUTSIDE_POINTS_CHANGE;

        private CompetitorPoints changeInPoints = new CompetitorPoints();

        public void setPointsChange(CompetitorPoints changeInPoints) {
            this.changeInPoints = changeInPoints;
        }

        public CompetitorPoints getPointsChange() {
            return changeInPoints;
        }
    }
}
