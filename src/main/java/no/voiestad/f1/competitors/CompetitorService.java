package no.voiestad.f1.competitors;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import no.voiestad.f1.competitors.constructor.*;
import no.voiestad.f1.competitors.domain.Color;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.competitors.driver.*;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;


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
        driverRepository.save(new DriverEntity(new DriverId(driverRepository.getNextId()), new DriverName(driver), year, position));
    }

    public int getMaxPosDriverYear(Year year) {
        return getDriversYear(year).size();
    }

    public void deleteDriver(DriverId driverId) {
        driverRepository.deleteById(driverId);
    }

    public void addConstructorYear(String constructor, Year year) {
        int position = getMaxPosConstructorYear(year) + 1;
        constructorRepository.save(new ConstructorEntity(
                new ConstructorId(constructorRepository.getNextId()), new ConstructorName(constructor), year, position));
    }

    public int getMaxPosConstructorYear(Year year) {
        return getConstructorsYear(year).size();
    }

    public void deleteConstructor(ConstructorId constructorId) {
        constructorRepository.deleteById(constructorId);
    }

    public void setTeamDriver(DriverId driverId, ConstructorEntity team) {
        driverTeamRepository.save(new DriverTeamEntity(driverId, team));
    }

    public List<DriverDTO> getDriversTeam(Year year) {
        return driverRepository.findAllByYearOrderByPosition(year).stream()
                .map(DriverDTO::fromEntityWithTeam)
                .toList();
    }

    public void addColorConstructor(ConstructorId constructorId, Color color) {
        constructorColorRepository.save(new ConstructorColorEntity(constructorId, color));
    }

    public List<ConstructorDTO> getConstructorsYearWithColors(Year year) {
        return constructorRepository.findAllByYearOrderByPosition(year).stream()
                .map(ConstructorDTO::fromEntityWithColor)
                .toList();
    }

    public Optional<DriverEntity> getDriver(int driverId) {
        return driverRepository.findById(new DriverId(driverId));
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
        return driverRepository.findByDriverNameAndYear(new DriverName(name), year);
    }

    public Optional<ConstructorEntity> getConstructorByNameAndYear(String name, Year year) {
        return constructorRepository.findByConstructorNameAndYear(new ConstructorName(name), year);
    }

    public List<DriverEntity> getAllDrivers(List<Integer> drivers) {
        return drivers.stream().map(this::getDriver).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public List<ConstructorEntity> getAllConstructors(List<Integer> constructors) {
        return constructors.stream().map(this::getConstructor).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public boolean renameDriver(DriverEntity driver, String name) {
        DriverName newName = new DriverName(name);
        if (driverRepository.findByDriverNameAndYear(newName, driver.year()).isPresent()) {
            return false;
        }
        driverRepository.save(driver.withName(newName));
        return true;
    }

    public boolean renameConstructor(ConstructorEntity constructor, String name) {
        ConstructorName newName = new ConstructorName(name);
        if (constructorRepository.findByConstructorNameAndYear(newName, constructor.year()).isPresent()) {
            return false;
        }
        constructorRepository.save(constructor.withName(newName));
        return true;
    }


    public <T> Optional<List<DriverEntity>> extractDrivers(
            List<T> values,
            Function<T, Integer> extractor,
            Year year) {
        int expectedLength = values.size();
        List<DriverEntity> drivers = values.stream()
                .map(extractor)
                .map(this::getDriver)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(driver -> driver.year().equals(year))
                .toList();
        if (drivers.size() != expectedLength || new HashSet<>(drivers).size() != expectedLength) {
            return Optional.empty();
        }
        return Optional.of(drivers);
    }

    public <T> Optional<List<ConstructorEntity>> extractConstructors(
            List<T> values,
            Function<T, Integer> extractor,
            Year year) {
        int expectedLength = values.size();
        List<ConstructorEntity> constructors = values.stream()
                .map(extractor)
                .map(this::getConstructor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(constructor -> constructor.year().equals(year))
                .toList();
        if (constructors.size() != expectedLength || new HashSet<>(constructors).size() != expectedLength) {
            return Optional.empty();
        }
        return Optional.of(constructors);
    }
}
