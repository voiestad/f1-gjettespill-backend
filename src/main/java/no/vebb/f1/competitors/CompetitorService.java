package no.vebb.f1.competitors;

import jakarta.persistence.EntityManager;
import no.vebb.f1.competitors.constructor.*;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.competitors.driver.*;
import no.vebb.f1.collection.ColoredCompetitor;
import no.vebb.f1.collection.ValuedCompetitor;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CompetitorService {

    private final DriverYearRepository driverYearRepository;
    private final ConstructorYearRepository constructorYearRepository;
    private final DriverRepository driverRepository;
    private final EntityManager entityManager;
    private final ConstructorRepository constructorRepository;
    private final DriverAlternativeNameRepository driverAlternativeNameRepository;
    private final DriverTeamRepository driverTeamRepository;
    private final ConstructorColorRepository constructorColorRepository;

    public CompetitorService(
            DriverYearRepository driverYearRepository,
            ConstructorYearRepository constructorYearRepository,
            DriverRepository driverRepository,
            EntityManager entityManager,
            ConstructorRepository constructorRepository,
            DriverAlternativeNameRepository driverAlternativeNameRepository,
            DriverTeamRepository driverTeamRepository,
            ConstructorColorRepository constructorColorRepository) {
        this.driverYearRepository = driverYearRepository;
        this.constructorYearRepository = constructorYearRepository;
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
                .toList();
    }

    public List<Constructor> getConstructorsYear(Year year) {
        return constructorYearRepository.findAllByIdYearOrderByPosition(year).stream()
                .map(ConstructorYearEntity::constructorName)
                .toList();
    }

    private void addDriver(String driver) {
        driverRepository.save(new DriverEntity(new Driver(driver)));
    }

    public Driver addDriverYear(String driver, Year year) {
        addDriver(driver);
        int position = getMaxPosDriverYear(year) + 1;
        driverYearRepository.save(new DriverYearEntity(new Driver(driver), year, position));
        return new Driver(driver);
    }

    public int getMaxPosDriverYear(Year year) {
        List<DriverYearEntity> drivers = driverYearRepository.findAllByIdYearOrderByPosition(year);
        if (drivers.isEmpty()) {
            return 0;
        }
        return drivers.get(drivers.size() - 1).position();
    }

    public void deleteDriverYear(Driver driver, Year year) {
        driverYearRepository.deleteById(new DriverId(driver, year));
        entityManager.flush();
        entityManager.clear();
        if (!driverYearRepository.existsByIdDriverName(driver)) {
            driverRepository.deleteById(driver);
        }
    }

    private void addConstructor(String constructor) {
        constructorRepository.save(new ConstructorEntity(new Constructor(constructor)));
    }

    public void addConstructorYear(String constructor, Year year) {
        addConstructor(constructor);
        int position = getMaxPosConstructorYear(year) + 1;
        constructorYearRepository.save(new ConstructorYearEntity(new Constructor(constructor), year, position));
    }

    public int getMaxPosConstructorYear(Year year) {
        List<ConstructorYearEntity> constructors = constructorYearRepository.findAllByIdYearOrderByPosition(year);
        if (constructors.isEmpty()) {
            return 0;
        }
        return constructors.get(constructors.size() - 1).position();
    }

    public void deleteConstructorYear(Constructor constructor, Year year) {
        constructorYearRepository.deleteById(new ConstructorId(constructor, year));
        entityManager.flush();
        entityManager.clear();
        if (!constructorYearRepository.existsByIdConstructorName(constructor)) {
            constructorRepository.deleteById(constructor);
        }
    }

    public Optional<Driver> getAltDriverName(String driverName, Year year) {
        Optional<Driver> optDriverAltName = driverAlternativeNameRepository
                .findById(new DriverAlternativeNameId(driverName, year))
                .map(DriverAlternativeNameEntity::driverName);
        if (optDriverAltName.isPresent()) {
            return optDriverAltName;
        }
        return getDriver(driverName).filter(driver -> isDriverInYear(driver, year));
    }

    public Driver getDriverNameOrAdd(String driver, Year year) {
        return getAltDriverName(driver, year).orElseGet(() -> addDriverYear(driver, year));
    }

    public Map<String, Driver> getAlternativeDriverNamesYear(Year year) {
        List<DriverAlternativeNameEntity> altNames = driverAlternativeNameRepository.findAllByIdYear(year);
        Map<String, Driver> linkedMap = new LinkedHashMap<>();
        for (DriverAlternativeNameEntity altName : altNames) {
            linkedMap.put(altName.alternativeName(), altName.driverName());
        }
        return linkedMap;
    }

    public void addAlternativeDriverName(Driver driver, String alternativeName, Year year) {
        driverAlternativeNameRepository.save(new DriverAlternativeNameEntity(alternativeName, year, driver));
    }

    public void deleteAlternativeName(Driver driver, Year year, String alternativeName) {
        driverAlternativeNameRepository.delete(new DriverAlternativeNameEntity(alternativeName, year, driver));
    }

    public void setTeamDriver(Driver driver, Constructor team, Year year) {
        driverTeamRepository.save(new DriverTeamEntity(driver, year, team));
    }

    public List<ValuedCompetitor<Driver, Constructor>> getDriversTeam(Year year) {
        return driverYearRepository.findAllByIdYearOrderByPosition(year).stream()
                .map(row -> new ValuedCompetitor<>(
                        row.driverName(),
                        row.team()))
                .toList();
    }

    public void addColorConstructor(Constructor constructor, Year year, Color color) {
        constructorColorRepository.save(new ConstructorColorEntity(constructor, year, color));
    }

    public List<ColoredCompetitor<Constructor>> getConstructorsYearWithColors(Year year) {
        return constructorYearRepository.findAllByIdYearOrderByPosition(year).stream()
                .map(row -> new ColoredCompetitor<>(
                        row.constructorName(),
                        row.color()))
                .toList();
    }

    public Optional<Driver> getDriver(String driverName) {
        return driverRepository.findById(new Driver(driverName)).map(DriverEntity::driverName);
    }

    public boolean isDriverInYear(Driver driver, Year year) {
        return driverYearRepository.existsById(new DriverId(driver, year));
    }

    public Optional<Constructor> getConstructor(String constructorName) {
        return constructorRepository.findById(new Constructor(constructorName)).map(ConstructorEntity::constructorName);
    }

    public boolean isConstructorInYear(Constructor constructor, Year year) {
        return constructorYearRepository.existsById(new ConstructorId(constructor, year));
    }

    public void setDriverYearOrder(List<DriverYearEntity> newOrder) {
        driverYearRepository.saveAll(newOrder);
    }

    public void setConstructorYearOrder(List<ConstructorYearEntity> newOrder) {
        constructorYearRepository.saveAll(newOrder);
    }
}
