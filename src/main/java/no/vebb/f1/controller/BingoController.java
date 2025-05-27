package no.vebb.f1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.BingoSquare;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class BingoController {
	
	final String REGEX = "[^A-Za-z0-9æøåÆØÅ,.'\"\\- ]";

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping("/api/public/bingo")
	public ResponseEntity<List<BingoSquare>> getCurrentBingoCard() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			return new ResponseEntity<>(db.getBingoCard(year), HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/bingomaster")
	public ResponseEntity<Boolean> isBingoMaster() {
		return new ResponseEntity<>(userService.isBingomaster(), HttpStatus.OK);
	}

	@PostMapping("/bingomaster/add-card/{year}")
	@Transactional
	public ResponseEntity<?> addBingoSquare(@PathVariable("year") int year) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			if (db.isBingoCardAdded(validSeason)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			for (int id = 0; id < 25; id++) {
				BingoSquare bingoSquare = new BingoSquare("", false, id, validSeason);
				db.addBingoSquare(bingoSquare);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/bingomaster/set/{year}")
	@Transactional
	public ResponseEntity<?> updateBingoSquareText(@PathVariable("year") int year,
		@RequestParam("id") int id, @RequestParam("text") String text) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			if (!db.isBingoCardAdded(validSeason)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			if (id < 0 || id >= 25) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			String validatedText = validate(text);
			db.setTextBingoSquare(validSeason, id, validatedText);;
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/bingomaster/mark/{year}")
	@Transactional
	public ResponseEntity<?> markBingoSquare(@PathVariable("year") int year, 
		@RequestParam("id") int id) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			db.toogleMarkBingoSquare(validSeason, id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private String validate(String text) {
		text = text.strip();
		text = text.replaceAll(REGEX, "");
		return text;
	}
}
