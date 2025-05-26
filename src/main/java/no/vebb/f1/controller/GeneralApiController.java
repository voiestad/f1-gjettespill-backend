package no.vebb.f1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.Race;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
@RequestMapping("/api/public")
public class GeneralApiController {

	@Autowired
	private Database db;
	
	@GetMapping("/year/list")
	public ResponseEntity<List<Integer>> listYears() {
		List<Integer> res = db.getAllValidYears().stream()
			.map(year -> year.value)
			.toList();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@GetMapping("/race/list/{year}")
	public ResponseEntity<List<Race>> listRaces(@PathVariable("year") int year) {
		try {
			Year validYear = new Year(year);
			return new ResponseEntity<>(db.getRacesYear(validYear), HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
