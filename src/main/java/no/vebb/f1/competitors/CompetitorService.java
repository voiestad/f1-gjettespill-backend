package no.vebb.f1.competitors;

import jakarta.persistence.EntityManager;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.collection.ValuedCompetitor;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.InvalidRaceException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitorService {

    private final DriverYearRepository driverYearRepository;
    private final ConstructorYearRepository constructorYearRepository;
    private final RaceService raceService;
    private final DriverRepository driverRepository;
    private final EntityManager entityManager;
    private final ConstructorRepository constructorRepository;
    private final DriverAlternativeNameRepository driverAlternativeNameRepository;
    private final DriverTeamRepository driverTeamRepository;
    private final ConstructorColorRepository constructorColorRepository;

    public CompetitorService(
            DriverYearRepository driverYearRepository,
            ConstructorYearRepository constructorYearRepository,
            RaceService raceService,
            DriverRepository driverRepository,
            EntityManager entityManager,
            ConstructorRepository constructorRepository,
            DriverAlternativeNameRepository driverAlternativeNameRepository,
            DriverTeamRepository driverTeamRepository,
            ConstructorColorRepository constructorColorRepository) {
        this.driverYearRepository = driverYearRepository;
        this.constructorYearRepository = constructorYearRepository;
        this.raceService = raceService;
        this.driverRepository = driverRepository;
        this.entityManager = entityManager;
        this.constructorRepository = constructorRepository;
        this.driverAlternativeNameRepository = driverAlternativeNameRepository;
        this.driverTeamRepository = driverTeamRepository;
        this.constructorColorRepository = constructorColorRepository;
    }

    public List<Driver> getDriversYear(Year year) {
        return driverYearRepository.findAllByIdYearOrderByPosition(year).stream()
                .map(DriverYearEntity::driverName)
                .map(Driver::new)
                .toList();
    }

    public List<Constructor> getConstructorsYear(Year year) {
        return constructorYearRepository.findAllByIdYearOrderByPosition(year).stream()
                .map(ConstructorYearEntity::constructorName)
                .map(Constructor::new)
                .toList();
    }

    public void addDriver(String driver) {
        driverRepository.save(new DriverEntity(driver));
    }

    public void addDriverYear(String driver, Year year) {
        addDriver(driver);
        int position = getMaxPosDriverYear(year) + 1;
        driverYearRepository.save(new DriverYearEntity(driver, year, position));
    }

    public int getMaxPosDriverYear(Year year) {
        List<DriverYearEntity> drivers = driverYearRepository.findAllByIdYearOrderByPosition(year);
        if (drivers.isEmpty()) {
            return 0;
        }
        return drivers.get(drivers.size() - 1).position();
    }

    public void updatePositionDriverYear(Driver driver, Year year, int position) {
        driverYearRepository.updatePosition(driver.value, year, position);
    }

    public void deleteDriverYear(Driver driver, Year year) {
        driverYearRepository.deleteById(new DriverId(driver.value, year));
        entityManager.flush();
        entityManager.clear();
        if (!driverYearRepository.existsByIdDriverName(driver.value)) {
            driverRepository.deleteById(driver.value);
        }
    }

    public void addConstructor(String constructor) {
        constructorRepository.save(new ConstructorEntity(constructor));
    }

    public void addConstructorYear(String constructor, Year year) {
        addConstructor(constructor);
        int position = getMaxPosConstructorYear(year) + 1;
        constructorYearRepository.save(new ConstructorYearEntity(constructor, year, position));
    }

    public int getMaxPosConstructorYear(Year year) {
        List<ConstructorYearEntity> constructors = constructorYearRepository.findAllByIdYearOrderByPosition(year);
        if (constructors.isEmpty()) {
            return 0;
        }
        return constructors.get(constructors.size() - 1).position();
    }

    public void updatePositionConstructorYear(Constructor constructor, Year year, int position) {
        constructorYearRepository.updatePosition(constructor.value, year, position);
    }

    public void deleteConstructorYear(Constructor constructor, Year year) {
        constructorYearRepository.deleteById(new ConstructorId(constructor.value, year));
        entityManager.flush();
        entityManager.clear();
        if (!constructorYearRepository.existsByIdConstructorName(constructor.value)) {
            constructorRepository.deleteById(constructor.value);
        }
    }

    public String getAlternativeDriverName(String driver, Year year) {
        return driverAlternativeNameRepository.findById(new DriverAlternativeNameId(driver, year))
                .map(DriverAlternativeNameEntity::driverName).orElse(driver);
    }

    public Map<String, String> getAlternativeDriverNamesYear(Year year) {
        List<DriverAlternativeNameEntity> altNames = driverAlternativeNameRepository.findAllByIdYear(year);
        Map<String, String> linkedMap = new LinkedHashMap<>();
        for (DriverAlternativeNameEntity altName : altNames) {
            linkedMap.put(altName.alternativeName(), altName.driverName());
        }
        return linkedMap;
    }

    public String getAlternativeDriverName(String driver, RaceId raceId) {
        try {
            Year year = raceService.getYearFromRaceId(raceId);
            return getAlternativeDriverName(driver, year);
        } catch (InvalidRaceException ignored) {
            return driver;
        }
    }

    public void addAlternativeDriverName(Driver driver, String alternativeName, Year year) {
        driverAlternativeNameRepository.save(new DriverAlternativeNameEntity(alternativeName, year, driver.value));
    }

    public void deleteAlternativeName(Driver driver, Year year, String alternativeName) {
        driverAlternativeNameRepository.delete(new DriverAlternativeNameEntity(alternativeName, year, driver.value));
    }

    public void setTeamDriver(Driver driver, Constructor team, Year year) {
        driverTeamRepository.save(new DriverTeamEntity(driver.value, year, team.value));
    }

    public List<ValuedCompetitor<Driver, Constructor>> getDriversTeam(Year year) {
        return driverTeamRepository.findAllByIdYearOrderByDriverYearPosition(year).stream()
                .map(row -> new ValuedCompetitor<>(
                        new Driver(row.driverName()),
                        new Constructor(row.team())))
                .toList();
    }

    public void addColorConstructor(Constructor constructor, Year year, Color color) {
        constructorColorRepository.save(new ConstructorColorEntity(constructor.value, year, color.toValue()));
    }

    public List<ColoredCompetitor<Constructor>> getConstructorsYearWithColors(Year year) {
        return constructorColorRepository.findAllByIdYearOrderByConstructorYearPosition(year).stream()
                .map(row -> new ColoredCompetitor<>(
                        new Constructor(row.constructorName()),
                        new Color(row.color())))
                .toList();
    }

    public boolean isValidDriverYear(Driver driver, Year year) {
        return driverYearRepository.existsById(new DriverId(driver.value, year));
    }

    public boolean isValidDriver(Driver driver) {
        return driverRepository.existsById(driver.value);
    }

    public boolean isValidConstructorYear(Constructor constructor, Year year) {
        return constructorYearRepository.existsById(new ConstructorId(constructor.value, year));
    }

    public boolean isValidConstructor(Constructor constructor) {
        return constructorRepository.existsById(constructor.value);
    }
}
