package no.vebb.f1.competitors;

import no.vebb.f1.competitors.constructor.*;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.competitors.driver.*;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitorService {

    private final DriverRepository driverRepository;
    private final ConstructorRepository constructorRepository;
    private final DriverTeamRepository driverTeamRepository;
    private final ConstructorColorRepository constructorColorRepository;

    public CompetitorService(
            DriverRepository driverRepository,
            ConstructorRepository constructorRepository,
            DriverTeamRepository driverTeamRepository,
            ConstructorColorRepository constructorColorRepository) {
        this.driverRepository = driverRepository;
        this.constructorRepository = constructorRepository;
        this.driverTeamRepository = driverTeamRepository;
        this.constructorColorRepository = constructorColorRepository;
    }

    public List<DriverEntity> getDriversYear(Year year) {
        return driverRepository.findAllByYearOrderByPosition(year);
    }

    public List<ConstructorEntity> getConstructorsYear(Year year) {
        return constructorRepository.findAllByYearOrderByPosition(year);
    }

    public void addDriverYear(String driver, Year year) {
        int position = getMaxPosDriverYear(year) + 1;
        driverRepository.save(new DriverEntity(new Driver(driver), year, position));
    }

    public int getMaxPosDriverYear(Year year) {
        List<DriverEntity> drivers = driverRepository.findAllByYearOrderByPosition(year);
        if (drivers.isEmpty()) {
            return 0;
        }
        return drivers.get(drivers.size() - 1).position();
    }

    public void deleteDriver(DriverId driverId) {
        driverRepository.deleteById(driverId);
    }

    public void addConstructorYear(String constructor, Year year) {
        int position = getMaxPosConstructorYear(year) + 1;
        constructorRepository.save(new ConstructorEntity(new Constructor(constructor), year, position));
    }

    public int getMaxPosConstructorYear(Year year) {
        List<ConstructorEntity> constructors = constructorRepository.findAllByYearOrderByPosition(year);
        if (constructors.isEmpty()) {
            return 0;
        }
        return constructors.get(constructors.size() - 1).position();
    }

    public void deleteConstructor(ConstructorId constructorId) {
        constructorRepository.deleteById(constructorId);
    }

    public void setTeamDriver(DriverId driverId, ConstructorId team) {
        driverTeamRepository.save(new DriverTeamEntity(driverId, team));
    }

    public List<DriverEntity> getDriversTeam(Year year) {
        List<DriverEntity> drivers = driverRepository.findAllByYearOrderByPosition(year);
        drivers.forEach(DriverEntity::team);
        return drivers;
    }

    public void addColorConstructor(ConstructorId constructorId, Color color) {
        constructorColorRepository.save(new ConstructorColorEntity(constructorId, color));
    }

    public List<ConstructorEntity> getConstructorsYearWithColors(Year year) {
        List<ConstructorEntity> constructors = constructorRepository.findAllByYearOrderByPosition(year);
        constructors.forEach(ConstructorEntity::color);
        return constructors;
    }

    public Optional<DriverEntity> getDriver(int driverId) {
        return driverRepository.findById(new DriverId(driverId));
    }

    public boolean isDriverInYear(DriverId driverId, Year year) {
        return driverRepository.findById(driverId)
                .filter(driver -> driver.year().equals(year)).isPresent();
    }

    public Optional<ConstructorEntity> getConstructor(int constructorId) {
        return constructorRepository.findById(new ConstructorId(constructorId));
    }

    public void setDriverOrder(List<DriverEntity> newOrder) {
        driverRepository.saveAll(newOrder);
    }

    public void setConstructorYearOrder(List<ConstructorEntity> newOrder) {
        constructorRepository.saveAll(newOrder);
    }

    public Optional<DriverEntity> getDriverByNameAndYear(String name, Year year) {
        return driverRepository.findByDriverNameAndYear(new Driver(name), year);
    }

    public Optional<ConstructorEntity> getConstructorByNameAndYear(String name, Year year) {
        return constructorRepository.findByConstructorNameAndYear(new Constructor(name), year);
    }

    public List<DriverEntity> getAllDrivers(List<Integer> drivers) {
        return driverRepository.findAllById(drivers.stream().map(DriverId::new).toList());
    }

        public List<ConstructorEntity> getAllConstructors(List<Integer> constructors) {
        return constructorRepository.findAllById(constructors.stream().map(ConstructorId::new).toList());
    }
}
