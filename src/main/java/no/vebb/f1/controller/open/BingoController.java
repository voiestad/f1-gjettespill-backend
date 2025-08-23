package no.vebb.f1.controller.open;

import java.util.List;

import no.vebb.f1.util.exception.YearFinishedException;
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

    private final Database db;
	private final UserService userService;

	public BingoController(Database db, UserService userService) {
		this.db = db;
		this.userService = userService;
	}

	@GetMapping("/api/public/bingo")
	public ResponseEntity<List<BingoSquare>> getCurrentBingoCard() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			return new ResponseEntity<>(db.getBingoCard(year), HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/api/public/bingo/{year}")
	public ResponseEntity<List<BingoSquare>> getBingoCardYear(@PathVariable("year") int year) {
		try {
			Year validYear = new Year(year, db);
			return new ResponseEntity<>(db.getBingoCard(validYear), HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/api/public/bingomaster")
	public ResponseEntity<Boolean> isBingoMaster() {
		return new ResponseEntity<>(userService.isBingomaster(), HttpStatus.OK);
	}

	@PostMapping("/api/bingomaster/add-card")
	@Transactional
	public ResponseEntity<?> addBingoSquare(@RequestParam("year") int year) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			if (db.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
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

	@PostMapping("/api/bingomaster/set")
	@Transactional
	public ResponseEntity<?> updateBingoSquareText(@RequestParam("year") int year,
		@RequestParam("id") int id, @RequestParam("text") String text) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			if (db.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
			if (!db.isBingoCardAdded(validSeason)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			if (id < 0 || id >= 25) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			String validatedText = validate(text);
			db.setTextBingoSquare(validSeason, id, validatedText);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/api/bingomaster/mark")
	@Transactional
	public ResponseEntity<?> markBingoSquare(@RequestParam("year") int year,
		@RequestParam("id") int id) {
		if (!userService.isBingomaster()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			Year validSeason = new Year(year, db);
			if (db.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
			db.toogleMarkBingoSquare(validSeason, id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private String validate(String text) {
        String REGEX = "[^A-Za-z0-9æøåÆØÅ,.'\"\\- ]";
		text = text.strip();
        text = text.replaceAll(REGEX, "");
		return text;
	}
}
