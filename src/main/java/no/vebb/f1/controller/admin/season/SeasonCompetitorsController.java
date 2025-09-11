package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.constructor.ConstructorYearEntity;
import no.vebb.f1.competitors.driver.DriverYearEntity;
import no.vebb.f1.collection.ColoredCompetitor;
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
    public ResponseEntity<List<ValuedCompetitor<Driver, Constructor>>> listDrivers(@PathVariable("year") Year year) {
        List<ValuedCompetitor<Driver, Constructor>> drivers = competitorService.getDriversTeam(year);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @PostMapping("/drivers/set-team")
    @Transactional
    public ResponseEntity<?> setTeamDriver(
            @RequestParam("year") Year year,
            @RequestParam("driver") String driver,
            @RequestParam("team") String team) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Driver> optDriver = competitorService.getDriver(driver, year);
        Optional<Constructor> optConstructor = competitorService.getConstructor(team, year);
        if (optDriver.isPresent() && optConstructor.isPresent()) {
            competitorService.setTeamDriver(optDriver.get(), optConstructor.get(), year);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/add")
    @Transactional
    public ResponseEntity<?> addDriverToSeason(@RequestParam("year") Year year, @RequestParam("driver") String driver) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (competitorService.getDriver(driver, year).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        competitorService.addDriverYear(driver, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/delete")
    @Transactional
    public ResponseEntity<?> removeDriverFromSeason(@RequestParam("year") Year year, @RequestParam("driver") String driver) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Driver> optDriver = competitorService.getDriver(driver, year);
        if (optDriver.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.deleteDriverYear(optDriver.get(), year);
        List<Driver> drivers = competitorService.getDriversYear(year);

        int position = 1;
        List<DriverYearEntity> newOrder = new ArrayList<>();
        for (Driver currentDriver : drivers) {
            newOrder.add(new DriverYearEntity(currentDriver, year, position++));
        }
        competitorService.setDriverYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/move")
    @Transactional
    public ResponseEntity<?> moveDriverFromSeason(
            @RequestParam("year") Year year,
            @RequestParam("driver") String driver,
            @RequestParam("newPosition") int position) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Driver> optDriver = competitorService.getDriver(driver, year);
        if (optDriver.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Driver validDriver = optDriver.get();
        int maxPos = competitorService.getMaxPosDriverYear(year);
        boolean isPosOutOfBounds = position < 1 || position > maxPos;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Driver> drivers = competitorService.getDriversYear(year);
        List<DriverYearEntity> newOrder = new ArrayList<>();
        int currentPos = 1;
        for (Driver currentDriver : drivers) {
            if (currentDriver.equals(validDriver)) {
                continue;
            }
            if (currentPos == position) {
                newOrder.add(new DriverYearEntity(validDriver, year, currentPos++));
            }
            newOrder.add(new DriverYearEntity(currentDriver, year, currentPos++));
        }
        if (currentPos == position) {
            newOrder.add(new DriverYearEntity(validDriver, year, currentPos));
        }
        competitorService.setDriverYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/constructors/list/{year}")
    public ResponseEntity<List<ColoredCompetitor<Constructor>>> listConstructors(@PathVariable("year") Year year) {
        List<ColoredCompetitor<Constructor>> constructors = competitorService.getConstructorsYearWithColors(year);
        return new ResponseEntity<>(constructors, HttpStatus.OK);
    }

    @PostMapping("/constructors/add")
    @Transactional
    public ResponseEntity<?> addConstructorToSeason(
            @RequestParam("year") Year year,
            @RequestParam("constructor") String constructor) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (competitorService.getConstructor(constructor, year).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        competitorService.addConstructorYear(constructor, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/delete")
    @Transactional
    public ResponseEntity<?> removeConstructorFromSeason(
            @RequestParam("year") Year year,
            @RequestParam("constructor") String constructor) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Constructor> validConstructor = competitorService.getConstructor(constructor);
        if (validConstructor.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.deleteConstructorYear(validConstructor.get(), year);
        List<Constructor> constructors = competitorService.getConstructorsYear(year);

        int position = 1;
        List<ConstructorYearEntity> newOrder = new ArrayList<>();
        for (Constructor currentConstructor : constructors) {
            newOrder.add(new ConstructorYearEntity(currentConstructor, year, position++));
        }
        competitorService.setConstructorYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/move")
    @Transactional
    public ResponseEntity<?> moveConstructorFromSeason(
            @RequestParam("year") Year year,
            @RequestParam("constructor") String constructor,
            @RequestParam("newPosition") int position) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Constructor> optConstructor = competitorService.getConstructor(constructor);
        if (optConstructor.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Constructor validConstructor = optConstructor.get();
        int maxPos = competitorService.getMaxPosConstructorYear(year);
        boolean isPosOutOfBounds = position < 1 || position > maxPos;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Constructor> constructors = competitorService.getConstructorsYear(year);
        List<ConstructorYearEntity> newOrder = new ArrayList<>();
        int currentPos = 1;
        for (Constructor currentConstructor : constructors) {
            if (currentConstructor.equals(validConstructor)) {
                continue;
            }
            if (currentPos == position) {
                newOrder.add(new ConstructorYearEntity(validConstructor, year, currentPos++));
            }
            newOrder.add(new ConstructorYearEntity(currentConstructor, year, currentPos++));
        }
        if (currentPos == position) {
            newOrder.add(new ConstructorYearEntity(validConstructor, year, currentPos));
        }
        competitorService.setConstructorYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/add-color")
    @Transactional
    public ResponseEntity<?> addColorConstructor(
            @RequestParam("year") Year year,
            @RequestParam("constructor") String constructor,
            @RequestParam("inputColor") String inputColor) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Color> optColor = Color.getColor(inputColor);
        if (optColor.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Constructor> optConstructor = competitorService.getConstructor(constructor, year);
        if (optConstructor.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.addColorConstructor(optConstructor.get(), year, optColor.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/alias/list/{year}")
    public ResponseEntity<Map<String, Driver>> addAlternativeNameForm(@PathVariable("year") Year year) {
        Map<String, Driver> driverAliases = competitorService.getAlternativeDriverNamesYear(year);
        return new ResponseEntity<>(driverAliases, HttpStatus.OK);
    }

    @PostMapping("/alias/add")
    @Transactional
    public ResponseEntity<?> addAlternativeName(
            @RequestParam("year") Year year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Driver> optDriver = competitorService.getDriver(driver, year);
        if (optDriver.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.addAlternativeDriverName(optDriver.get(), alternativeName, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/alias/delete")
    @Transactional
    public ResponseEntity<?> deleteAlternativeName(
            @RequestParam("year") Year year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName
    ) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Driver> optDriver = competitorService.getDriver(driver, year);
        if (optDriver.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.deleteAlternativeName(optDriver.get(), year, alternativeName);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
