package no.vebb.f1.controller.admin.season;

import java.util.List;
import java.util.Map;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.util.collection.ValuedCompetitor;
import no.vebb.f1.util.domainPrimitive.Color;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidColorException;
import no.vebb.f1.util.exception.InvalidConstructorException;
import no.vebb.f1.util.exception.InvalidDriverException;

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
        Year seasonYear = new Year(year, yearService);
        List<ValuedCompetitor<Driver, Constructor>> drivers = competitorService.getDriversTeam(seasonYear);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @PostMapping("/drivers/set-team")
    @Transactional
    public ResponseEntity<?> setTeamDriver(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("team") String team) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.setTeamDriver(new Driver(driver, competitorService, validYear), new Constructor(team, competitorService, validYear), validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException | InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/drivers/add")
    @Transactional
    public ResponseEntity<?> addDriverToSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            new Driver(driver, competitorService, validYear);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidDriverException e) {
            competitorService.addDriverYear(driver, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/drivers/delete")
    @Transactional
    public ResponseEntity<?> removeDriverFromSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = new Driver(driver, competitorService, validYear);

            competitorService.deleteDriverYear(validDriver, validYear);
            List<Driver> drivers = competitorService.getDriversYear(validYear);

            int position = 1;
            for (Driver currentDriver : drivers) {
                competitorService.updatePositionDriverYear(currentDriver, validYear, position);
                position++;
            }
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
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = new Driver(driver, competitorService, validYear);
            int maxPos = competitorService.getMaxPosDriverYear(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            competitorService.deleteDriverYear(validDriver, validYear);
            List<Driver> drivers = competitorService.getDriversYear(validYear);

            int currentPos = 1;
            for (Driver currentDriver : drivers) {
                if (currentPos == position) {
                    competitorService.updatePositionDriverYear(validDriver, validYear, currentPos);
                    currentPos++;
                }
                competitorService.updatePositionDriverYear(currentDriver, validYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                competitorService.updatePositionDriverYear(validDriver, validYear, currentPos);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/constructors/list/{year}")
    public ResponseEntity<List<ColoredCompetitor<Constructor>>> listConstructors(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, yearService);
        List<ColoredCompetitor<Constructor>> constructors = competitorService.getConstructorsYearWithColors(seasonYear);
        return new ResponseEntity<>(constructors, HttpStatus.OK);
    }

    @PostMapping("/constructors/add")
    @Transactional
    public ResponseEntity<?> addConstructorToSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            new Constructor(constructor, competitorService, validYear);
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
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Constructor validConstructor = new Constructor(constructor, competitorService);

            competitorService.deleteConstructorYear(validConstructor, validYear);
            List<Constructor> constructors = competitorService.getConstructorsYear(validYear);

            int position = 1;
            for (Constructor currentConstructor : constructors) {
                competitorService.updatePositionConstructorYear(currentConstructor, validYear, position);
                position++;
            }
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
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Constructor validConstructor = new Constructor(constructor, competitorService);
            int maxPos = competitorService.getMaxPosConstructorYear(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            competitorService.deleteConstructorYear(validConstructor, validYear);
            List<Constructor> constructors = competitorService.getConstructorsYear(validYear);

            int currentPos = 1;
            for (Constructor currentConstructor : constructors) {
                if (currentPos == position) {
                    competitorService.updatePositionConstructorYear(validConstructor, validYear, currentPos);
                    currentPos++;
                }
                competitorService.updatePositionConstructorYear(currentConstructor, validYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                competitorService.updatePositionConstructorYear(validConstructor, validYear, currentPos);
            }
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
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            competitorService.addColorConstructor(new Constructor(constructor, competitorService, validYear), validYear, new Color(color));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException | InvalidColorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/alias/list/{year}")
    public ResponseEntity<Map<String, String>> addAlternativeNameForm(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, yearService);
        Map<String, String> driverAliases = competitorService.getAlternativeDriverNamesYear(seasonYear);
        return new ResponseEntity<>(driverAliases, HttpStatus.OK);
    }

    @PostMapping("/alias/add")
    @Transactional
    public ResponseEntity<?> addAlternativeName(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = new Driver(driver, competitorService, validYear);
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
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the competitors can't be changed");
        }
        try {
            Driver validDriver = new Driver(driver, competitorService, validYear);
            competitorService.deleteAlternativeName(validDriver, validYear, alternativeName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
