package no.vebb.f1.controller.admin.season;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
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

    @Autowired
    private Database db;

    @GetMapping("/drivers/list/{year}")
    public ResponseEntity<List<ValuedCompetitor<Driver, Constructor>>> listDrivers(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, db);
        List<ValuedCompetitor<Driver, Constructor>> drivers = db.getDriversTeam(seasonYear);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @PostMapping("/drivers/set-team")
    @Transactional
    public ResponseEntity<?> setTeamDriver(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("team") String team) {
        Year seasonYear = new Year(year, db);
        try {
            db.setTeamDriver(new Driver(driver, db, seasonYear), new Constructor(team, db, seasonYear), seasonYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException | InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/drivers/add")
    @Transactional
    public ResponseEntity<?> addDriverToSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year seasonYear = new Year(year, db);
        try {
            new Driver(driver, db, seasonYear);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidDriverException e) {
            db.addDriverYear(driver, seasonYear);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/drivers/delete")
    @Transactional
    public ResponseEntity<?> removeDriverFromSeason(@RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year seasonYear = new Year(year, db);
        try {
            Driver validDriver = new Driver(driver, db, seasonYear);

            db.deleteDriverYear(validDriver, seasonYear);
            List<Driver> drivers = db.getDriversYear(seasonYear);
            db.deleteAllDriverYear(seasonYear);

            int position = 1;
            for (Driver currentDriver : drivers) {
                db.addDriverYear(currentDriver, seasonYear, position);
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
        Year seasonYear = new Year(year, db);
        try {
            Driver validDriver = new Driver(driver, db, seasonYear);
            int maxPos = db.getMaxPosDriverYear(seasonYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            db.deleteDriverYear(validDriver, seasonYear);
            List<Driver> drivers = db.getDriversYear(seasonYear);
            db.deleteAllDriverYear(seasonYear);

            int currentPos = 1;
            for (Driver currentDriver : drivers) {
                if (currentPos == position) {
                    db.addDriverYear(validDriver, seasonYear, currentPos);
                    currentPos++;
                }
                db.addDriverYear(currentDriver, seasonYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                db.addDriverYear(validDriver, seasonYear, currentPos);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/constructors/list/{year}")
    public ResponseEntity<List<Constructor>> listConstructors(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, db);
        List<Constructor> constructors = db.getConstructorsYear(seasonYear);
        return new ResponseEntity<>(constructors, HttpStatus.OK);
    }

    @PostMapping("/constructors/add")
    @Transactional
    public ResponseEntity<?> addConstructorToSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor) {
        Year seasonYear = new Year(year, db);
        try {
            new Constructor(constructor, db, seasonYear);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidConstructorException e) {
            db.addConstructorYear(constructor, seasonYear);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/constructors/delete")
    @Transactional
    public ResponseEntity<?> removeConstructorFromSeason(
            @RequestParam("year") int year,
            @RequestParam("constructor") String constructor) {
        Year seasonYear = new Year(year, db);
        try {
            Constructor validConstructor = new Constructor(constructor, db);

            db.deleteConstructorYear(validConstructor, seasonYear);
            List<Constructor> constructors = db.getConstructorsYear(seasonYear);
            db.deleteAllConstructorYear(seasonYear);

            int position = 1;
            for (Constructor currentConstructor : constructors) {
                db.addConstructorYear(currentConstructor, seasonYear, position);
                position++;
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/constructors/move")
    @Transactional
    public ResponseEntity<?> moveConstructorFromSeason(@RequestParam("year") int year,
                                                       @RequestParam("constructor") String constructor, @RequestParam("newPosition") int position) {
        Year seasonYear = new Year(year, db);
        try {
            Constructor validConstructor = new Constructor(constructor, db);
            int maxPos = db.getMaxPosConstructorYear(seasonYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            db.deleteConstructorYear(validConstructor, seasonYear);
            List<Constructor> constructors = db.getConstructorsYear(seasonYear);
            db.deleteAllConstructorYear(seasonYear);

            int currentPos = 1;
            for (Constructor currentConstructor : constructors) {
                if (currentPos == position) {
                    db.addConstructorYear(validConstructor, seasonYear, currentPos);
                    currentPos++;
                }
                db.addConstructorYear(currentConstructor, seasonYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                db.addConstructorYear(validConstructor, seasonYear, currentPos);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/constructors/add-color")
    @Transactional
    public ResponseEntity<?> addColorConstructor(@RequestParam("year") int year, @RequestParam("constructor") String constructor,
                                                 @RequestParam("color") String color) {
        Year seasonYear = new Year(year, db);
        try {
            db.addColorConstructor(new Constructor(constructor, db, seasonYear), seasonYear, new Color(color));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidConstructorException | InvalidColorException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/alias/list/{year}")
    public ResponseEntity<Map<String, String>> addAlternativeNameForm(@PathVariable("year") int year) {
        Year seasonYear = new Year(year, db);
        Map<String, String> driverAliases = db.getAlternativeDriverNamesYear(seasonYear);
        return new ResponseEntity<>(driverAliases, HttpStatus.OK);
    }

    @PostMapping("/alias/add")
    @Transactional
    public ResponseEntity<?> addAlternativeName(
            @RequestParam("year") int year,
            @RequestParam("driver") String driver,
            @RequestParam("alternativeName") String alternativeName) {
        Year seasonYear = new Year(year, db);
        try {
            Driver validDriver = new Driver(driver, db, seasonYear);
            db.addAlternativeDriverName(validDriver, alternativeName, seasonYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/alias/delete")
    @Transactional
    public ResponseEntity<?> deleteAlternativeName(
            @RequestParam("year") int year, @RequestParam("driver") String driver) {
        Year seasonYear = new Year(year, db);
        try {
            Driver validDriver = new Driver(driver, db, seasonYear);
            db.deleteAlternativeName(validDriver, seasonYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDriverException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
