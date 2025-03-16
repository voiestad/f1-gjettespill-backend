package no.vebb.f1.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.PositionedCompetitor;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;

@Service
public class StatsUtil {

	@Autowired
	private Database db;


	public Table getStartingGridTable(RaceId raceId) {
		List<String> header = Arrays.asList("Plass", "Sjåfør");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> startingGrid = db.getStartingGrid(raceId);
		for (PositionedCompetitor driver : startingGrid) {
			body.add(Arrays.asList(driver.position, driver.name));
		}

		return new Table("Startoppstilling", header, body);
	}

	public Table getRaceResultTable(RaceId raceId) {
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> raceResult = db.getRaceResult(raceId);
		for (PositionedCompetitor driver : raceResult) {
			body.add(Arrays.asList(driver.position, driver.name, driver.points));
		}
		return new Table("Result av løp", header, body);
	}

	public Table getDriverStandingsTable(RaceId raceId) {
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> standings = db.getDriverStandings(raceId);
		for (PositionedCompetitor driver : standings) {
			body.add(Arrays.asList(driver.position, driver.name, driver.points));
		}

		return new Table("Sjåførmesterskap", header, body);
	}

	public Table getConstructorStandingsTable(RaceId raceId) {
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> standings = db.getConstructorStandings(raceId);
		for (PositionedCompetitor constructor : standings) {
			body.add(Arrays.asList(constructor.position, constructor.name, constructor.points));
		}

		return new Table("Konstruktørmesterskap", header, body);
	}

	public Table getFlagTable(RaceId raceId) {
		List<String> header = Arrays.asList("Runde", "Type");
		List<List<String>> body = new ArrayList<>();
		List<RegisteredFlag> flags = db.getRegisteredFlags(raceId);
		for (RegisteredFlag flag : flags) {
			body.add(Arrays.asList(String.valueOf(flag.round), db.translateFlagName(flag.type)));
		}
		return new Table("Flagg", header, body);
	}
}
