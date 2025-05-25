package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.StatsUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.response.LinkListResponse;
import no.vebb.f1.util.response.TablesResponse;

@RestController
@RequestMapping("/api/public/stats")
public class StatsController {

	@Autowired
	private Database db;

	@Autowired
	private StatsUtil statsUtil;

	@GetMapping("/links")
	public ResponseEntity<LinkListResponse> chooseYear() {
		LinkListResponse res = new LinkListResponse();
		Map<String, String> linkMap = new LinkedHashMap<>();
		res.title = "Statistikk";
		res.heading = "Velg år";
		res.links = linkMap;
		List<Year> years = db.getAllValidYears();
		for (Year year : years) {
			linkMap.put(String.valueOf(year), "/stats/" + year);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping("/{year}")
	public ResponseEntity<LinkListResponse> chooseRace(@PathVariable("year") int year) {
		try {
			LinkListResponse res = new LinkListResponse();
			Map<String, String> linkMap = new LinkedHashMap<>();
			res.title = "Statistikk";
			res.heading = "Velg løp";
			res.links = linkMap;
			Year seasonYear = new Year(year, db);
			List<CutoffRace> races = db.getCutoffRaces(seasonYear);
			for (CutoffRace race : races) {
				linkMap.put(race.position + ". " + race.name, "/stats/" + year + "/" + race.id);
			}
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/{year}/{raceId}")
	public ResponseEntity<TablesResponse> manageRacesInSeason(@PathVariable("raceId") int raceId, @PathVariable("year") int year) {
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			
			List<Table> tables = new ArrayList<>();
			tables.add(statsUtil.getStartingGridTable(validRaceId));
			tables.add(statsUtil.getRaceResultTable(validRaceId));
			tables.add(statsUtil.getDriverStandingsTable(validRaceId));
			tables.add(statsUtil.getConstructorStandingsTable(validRaceId));
			tables.add(statsUtil.getFlagTable(validRaceId));
			
			TablesResponse res = new TablesResponse();
			res.tables = tables;

			int position = db.getPositionOfRace(validRaceId);
			String raceName = db.getRaceName(validRaceId);
			String title = String.format("%d. %s %d", position, raceName, year);
			res.title = title;
			res.heading = title;
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidRaceException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
