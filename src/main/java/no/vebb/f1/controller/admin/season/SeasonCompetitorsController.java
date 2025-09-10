package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.constructor.ConstructorYearEntity;
import no.vebb.f1.competitors.driver.DriverYearEntity;
import no.vebb.f1.collection.ColoredCompetitor;
import no.vebb.f1.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.collection.ValuedCompetitor;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidColorException;
import no.vebb.f1.exception.InvalidConstructorException;
import no.vebb.f1.exception.InvalidDriverException;

@RestController
@RequestMapping("/api/admin/season/competitors")
public class SeasonCompetitorsController {

    private final YearService yearService;
    private final CompetitorService competitorService;

    public SeasonCompetitorsController(YearService yearService, CompetitorService competitorService) {
        this.yearService = yearService;
        this.competitorService = competitorService;
    }

    @GetMapping("/drivers/list/{year}")
    public ResponseEntity<List<ValuedCompetitor<Driver, Constructor>>> listDrivers(@PathVariable("year") int year) {
        Year seasonYear = yearService.getYear(year);
        List<ValuedCompetitor<Driver, Constructor>> drivers = competitorService.getDriversTeam(seasonYear);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @PostMapping("/drivers/set-team")
    @Transactional
    public ResponseEntity<?> setTeamDriver(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("team") String team) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.setTeamDriver(competitorService.getDriver(driver, validYear), competitorService.getConstructor(team, validYear), validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException | InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/drivers/add")
    @Transactional
    public ResponseEntity<?> addDriverToSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.getDriver(driver, validYear);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidDriverException e) {
            competitorService.addDriverYear(driver, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/drivers/delete")
    @Transactional
    public ResponseEntity<?> removeDriverFromSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = competitorService.getDriver(driver, validYear);

            competitorService.deleteDriverYear(validDriver, validYear);
            List<Driver> drivers = competitorService.getDriversYear(validYear);

            int position = 1;
            List<DriverYearEntity> newOrder = new ArrayList<>();
            for (Driver currentDriver : drivers) {
                newOrder.add(new DriverYearEntity(currentDriver, validYear, position++));
            }
            competitorService.setDriverYearOrder(newOrder);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/drivers/move")
    @Transactional
    public ResponseEntity<?> moveDriverFromSeason(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("newPosition") int position) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = competitorService.getDriver(driver, validYear);
            int maxPos = competitorService.getMaxPosDriverYear(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<Driver> drivers = competitorService.getDriversYear(validYear);
            List<DriverYearEntity> newOrder = new ArrayList<>();
            int currentPos = 1;
            for (Driver currentDriver : drivers) {
                if (currentDriver.equals(validDriver)) {
                    continue;
                }
                if (currentPos == position) {
                    newOrder.add(new DriverYearEntity(validDriver, validYear, currentPos++));
                }
                newOrder.add(new DriverYearEntity(currentDriver, validYear, currentPos++));
            }
            if (currentPos == position) {
                newOrder.add(new DriverYearEntity(validDriver, validYear, currentPos));
            }
            competitorService.setDriverYearOrder(newOrder);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/constructors/list/{year}")
    public ResponseEntity<List<ColoredCompetitor<Constructor>>> listConstructors(@PathVariable("year") int year) {
        Year seasonYear = yearService.getYear(year);
        List<ColoredCompetitor<Constructor>> constructors = competitorService.getConstructorsYearWithColors(seasonYear);
        return new ResponseEntity<>(constructors, HttpStatus.OK);
    }

    @PostMapping("/constructors/add")
    @Transactional
    public ResponseEntity<?> addConstructorToSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.getConstructor(constructor, validYear);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidConstructorException e) {
            competitorService.addConstructorYear(constructor, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/constructors/delete")
    @Transactional
    public ResponseEntity<?> removeConstructorFromSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Constructor validConstructor = competitorService.getConstructor(constructor);

            competitorService.deleteConstructorYear(validConstructor, validYear);
            List<Constructor> constructors = competitorService.getConstructorsYear(validYear);

            int position = 1;
            List<ConstructorYearEntity> newOrder = new ArrayList<>();
            for (Constructor currentConstructor : constructors) {
                newOrder.add(new ConstructorYearEntity(currentConstructor, validYear, position++));
            }
            competitorService.setConstructorYearOrder(newOrder);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/constructors/move")
    @Transactional
    public ResponseEntity<?> moveConstructorFromSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor,
            @RequestParam("newPosition") int position) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Constructor validConstructor = competitorService.getConstructor(constructor);
            int maxPos = competitorService.getMaxPosConstructorYear(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<Constructor> constructors = competitorService.getConstructorsYear(validYear);
            List<ConstructorYearEntity> newOrder = new ArrayList<>();
            int currentPos = 1;
            for (Constructor currentConstructor : constructors) {
                if (currentConstructor.equals(validConstructor)) {
                    continue;
                }
                if (currentPos == position) {
                    newOrder.add(new ConstructorYearEntity(validConstructor, validYear, currentPos++));
                }
                newOrder.add(new ConstructorYearEntity(currentConstructor, validYear, currentPos++));
            }
            if (currentPos == position) {
                newOrder.add(new ConstructorYearEntity(validConstructor, validYear, currentPos));
            }
            competitorService.setConstructorYearOrder(newOrder);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/constructors/add-color")
    @Transactional
    public ResponseEntity<?> addColorConstructor(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor,
            @RequestParam("color") String color) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.addColorConstructor(competitorService.getConstructor(constructor, validYear), validYear, new Color(color));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException | InvalidColorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/alias/list/{year}")
    public ResponseEntity<Map<String, Driver>> addAlternativeNameForm(@PathVariable("year") int year) {
        Year seasonYear = yearService.getYear(year);
        Map<String, Driver> driverAliases = competitorService.getAlternativeDriverNamesYear(seasonYear);
        return new ResponseEntity<>(driverAliases, HttpStatus.OK);
    }

    @PostMapping("/alias/add")
    @Transactional
    public ResponseEntity<?> addAlternativeName(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = competitorService.getDriver(driver, validYear);
            competitorService.addAlternativeDriverName(validDriver, alternativeName, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/alias/delete")
    @Transactional
    public ResponseEntity<?> deleteAlternativeName(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName
    ) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = competitorService.getDriver(driver, validYear);
            competitorService.deleteAlternativeName(validDriver, validYear, alternativeName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
