package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.constructor.ConstructorDTO;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.driver.DriverDTO;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.competitors.domain.Color;
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
    public ResponseEntity<List<DriverDTO>> listDrivers(@PathVariable("year") Year year) {
        return new ResponseEntity<>(competitorService.getDriversTeam(year), HttpStatus.OK);
    }

    @PostMapping("/drivers/set-team")
    @Transactional
    public ResponseEntity<?> setTeamDriver(
            @RequestParam("driver") DriverEntity driver,
            @RequestParam("team") ConstructorEntity team) {
        if (!driver.year().equals(team.year())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Year year = driver.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        competitorService.setTeamDriver(driver.driverId(), team);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/add")
    @Transactional
    public ResponseEntity<?> addDriverToSeason(@RequestParam("year") Year year, @RequestParam("driver") String driverName) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<DriverEntity> optDriver = competitorService.getDriverByNameAndYear(driverName, year);
        if (optDriver.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        competitorService.addDriverYear(driverName, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/delete")
    @Transactional
    public ResponseEntity<?> removeDriver(@RequestParam("driver") DriverEntity driver) {
        Year year = driver.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        competitorService.deleteDriver(driver.driverId());
        List<DriverEntity> drivers = competitorService.getDriversYear(year);

        int position = 1;
        List<DriverEntity> newOrder = new ArrayList<>();
        for (DriverEntity currentDriver : drivers) {
            newOrder.add(currentDriver.withPosition(position++));
        }
        competitorService.setDriverOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/move")
    @Transactional
    public ResponseEntity<?> moveDriverFromSeason(
            @RequestParam("driver") DriverEntity driver,
            @RequestParam("newPosition") int position) {
        Year year = driver.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        int maxPos = competitorService.getMaxPosDriverYear(year);
        boolean isPosOutOfBounds = position < 1 || position > maxPos;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<DriverEntity> drivers = competitorService.getDriversYear(year);
        List<DriverEntity> newOrder = new ArrayList<>();
        int currentPos = 1;
        for (DriverEntity currentDriver : drivers) {
            if (currentDriver.equals(driver)) {
                continue;
            }
            if (currentPos == position) {
                newOrder.add(driver.withPosition(currentPos++));
            }
            newOrder.add(currentDriver.withPosition(currentPos++));
        }
        if (currentPos == position) {
            newOrder.add(driver.withPosition(currentPos));
        }
        competitorService.setDriverOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drivers/rename")
    @Transactional
    public ResponseEntity<?> renameDriver(@RequestParam("driver") DriverEntity driver,
                                          @RequestParam("name") String name) {
        if (competitorService.renameDriver(driver, name)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @GetMapping("/constructors/list/{year}")
    public ResponseEntity<List<ConstructorDTO>> listConstructors(@PathVariable("year") Year year) {
        List<ConstructorDTO> constructors = competitorService.getConstructorsYearWithColors(year);
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
        Optional<ConstructorEntity> optConstructor = competitorService.getConstructorByNameAndYear(constructor, year);
        if (optConstructor.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        competitorService.addConstructorYear(constructor, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/delete")
    @Transactional
    public ResponseEntity<?> removeConstructor(
            @RequestParam("constructor") ConstructorEntity constructor) {
        Year year = constructor.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        competitorService.deleteConstructor(constructor.constructorId());
        List<ConstructorEntity> constructors = competitorService.getConstructorsYear(year);

        int position = 1;
        List<ConstructorEntity> newOrder = new ArrayList<>();
        for (ConstructorEntity currentConstructor : constructors) {
            newOrder.add(currentConstructor.withPosition(position++));
        }
        competitorService.setConstructorYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/move")
    @Transactional
    public ResponseEntity<?> moveConstructorFromSeason(
            @RequestParam("constructor") ConstructorEntity constructor,
            @RequestParam("newPosition") int position) {
        Year year = constructor.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        int maxPos = competitorService.getMaxPosConstructorYear(year);
        boolean isPosOutOfBounds = position < 1 || position > maxPos;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<ConstructorEntity> constructors = competitorService.getConstructorsYear(year);
        List<ConstructorEntity> newOrder = new ArrayList<>();
        int currentPos = 1;
        for (ConstructorEntity currentConstructor : constructors) {
            if (currentConstructor.equals(constructor)) {
                continue;
            }
            if (currentPos == position) {
                newOrder.add(constructor.withPosition(currentPos++));
            }
            newOrder.add(currentConstructor.withPosition(currentPos++));
        }
        if (currentPos == position) {
            newOrder.add(constructor.withPosition(currentPos));
        }
        competitorService.setConstructorYearOrder(newOrder);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/add-color")
    @Transactional
    public ResponseEntity<?> addColorConstructor(
            @RequestParam("constructor") ConstructorEntity constructor,
            @RequestParam("color") String inputColor) {
        Year year = constructor.year();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the competitors can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Color> optColor = Color.getColor(inputColor);
        if (optColor.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        competitorService.addColorConstructor(constructor.constructorId(), optColor.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/constructors/rename")
    @Transactional
    public ResponseEntity<?> renameConstructor(@RequestParam("constructor") ConstructorEntity constructor,
                                               @RequestParam("name") String name) {
        if (competitorService.renameConstructor(constructor, name)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
}
