package no.vebb.f1.controller.open;

import java.util.List;

import no.vebb.f1.bingo.BingoService;
import no.vebb.f1.year.YearService;
import no.vebb.f1.user.UserService;
import no.vebb.f1.bingo.BingoSquare;
import no.vebb.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return yearService.getCurrentYear()
                .flatMap(bingoService::getBingoCard)
                .map(bingoSquares -> new ResponseEntity<>(bingoSquares, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/api/public/bingo/{year}")
    public ResponseEntity<List<BingoSquare>> getBingoCardYear(@PathVariable("year") Year year) {
        return bingoService.getBingoCard(year)
                .map(bingoSquares -> new ResponseEntity<>(bingoSquares, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/api/public/bingomaster")
    public ResponseEntity<Boolean> isBingoMaster() {
        return new ResponseEntity<>(userService.isBingomaster(), HttpStatus.OK);
    }

    @PostMapping("/api/bingomaster/add-card")
    @Transactional
    public ResponseEntity<?> addBingoSquare(@RequestParam("year") Year year) {
        if (!userService.isBingomaster()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the bingo can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (bingoService.isBingoCardAdded(year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        bingoService.addBingoCard(year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/bingomaster/set")
    @Transactional
    public ResponseEntity<?> updateBingoSquareText(
            @RequestParam("year") Year year,
            @RequestParam("id") int id,
            @RequestParam("text") String text) {
        if (!userService.isBingomaster()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the bingo can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!bingoService.isBingoCardAdded(year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        String validatedText = validate(text);
        if (bingoService.setTextBingoSquare(year, id, validatedText)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/api/bingomaster/mark")
    @Transactional
    public ResponseEntity<?> markBingoSquare(
            @RequestParam("year") Year year,
            @RequestParam("id") int id) {
        if (!userService.isBingomaster()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the bingo can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!bingoService.toogleMarkBingoSquare(year, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String validate(String text) {
        String REGEX = "[^A-Za-z0-9æøåÆØÅ,.'\"\\- ]";
        text = text.strip();
        text = text.replaceAll(REGEX, "");
        return text;
    }
}
