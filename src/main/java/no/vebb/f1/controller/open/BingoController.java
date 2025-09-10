package no.vebb.f1.controller.open;

import java.util.List;

import no.vebb.f1.bingo.BingoService;
import no.vebb.f1.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.user.UserService;
import no.vebb.f1.bingo.BingoSquare;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidYearException;

@RestController
public class BingoController {

	private final UserService userService;
	private final YearService yearService;
	private final BingoService bingoService;

	public BingoController(UserService userService, YearService yearService, BingoService bingoService) {
		this.userService = userService;
		this.yearService = yearService;
		this.bingoService = bingoService;
	}

	@GetMapping("/api/public/bingo")
	public ResponseEntity<List<BingoSquare>> getCurrentBingoCard() {
		try {
			Year year = yearService.getCurrentYear();
			return new ResponseEntity<>(bingoService.getBingoCard(year), HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/api/public/bingo/{year}")
	public ResponseEntity<List<BingoSquare>> getBingoCardYear(@PathVariable("year") int year) {
		try {
			Year validYear = yearService.getYear(year);
			return new ResponseEntity<>(bingoService.getBingoCard(validYear), HttpStatus.OK);
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
			Year validSeason = yearService.getYear(year);
			if (yearService.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
			if (bingoService.isBingoCardAdded(validSeason)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			bingoService.addBingoCard(validSeason);
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
			Year validSeason = yearService.getYear(year);
			if (yearService.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
			if (!bingoService.isBingoCardAdded(validSeason)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			if (id < 0 || id >= 25) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			String validatedText = validate(text);
			bingoService.setTextBingoSquare(validSeason, id, validatedText);
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
			Year validSeason = yearService.getYear(year);
			if (yearService.isFinishedYear(validSeason)) {
				throw new YearFinishedException("Year '" + year + "' is over and the bingo can't be changed");
			}
			if (!bingoService.toogleMarkBingoSquare(validSeason, id)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
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
