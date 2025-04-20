package no.vebb.f1.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.BingoSquare;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/bingo")
public class BingoController {
	
	final String REGEX = "[^A-Za-z0-9æøåÆØÅ,.'\"\\- ]";

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String displayBingocard(Model model) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			model.addAttribute("bingoCard", db.getBingoCard(year));
			model.addAttribute("isBingomaster", userService.isBingomaster());
			model.addAttribute("title", "Bingo");
		} catch (InvalidYearException e) {
		}
		return "bingo/bingo";
	}

	@GetMapping("/admin")
	public String chooseBingoYear(Model model) {
		model.addAttribute("title", "Administrer bingo");
		Map<String, String> linkMap = new LinkedHashMap<>();
		model.addAttribute("linkMap", linkMap);
		List<Year> years = db.getAllValidYears();
		for (Year year : years) {
			linkMap.put(String.valueOf(year), "/bingo/admin/" + year);
		}
		return "util/linkList";
	}

	@GetMapping("/admin/{year}")
	public String administrateBingoCard(@PathVariable("year") int year, Model model) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			model.addAttribute("bingoCard", db.getBingoCard(validSeason));
			model.addAttribute("year", year);
			model.addAttribute("title", "Bingo " + year);
			return "bingo/bingoCardAdmin";
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/add-card")
	@Transactional
	public String addBingoSquare(@PathVariable("year") int year) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			if (db.isBingoCardAdded(validSeason)) {
				return "redirect:/bingo/admin/" + year;
			}
			for (int id = 0; id < 25; id++) {
				BingoSquare bingoSquare = new BingoSquare("", false, id, validSeason);
				db.addBingoSquare(bingoSquare);
			}
			return "redirect:/bingo/admin/" + year;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/set")
	@Transactional
	public String updateBingoSquareText(@PathVariable("year") int year,
		@RequestParam("id") int id, @RequestParam("text") String text) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			if (!db.isBingoCardAdded(validSeason)) {
				return "redirect:/bingo/admin/" + year;
			}
			if (id < 0 || id >= 25) {
				return "redirect:/bingo/admin/" + year;
			}
			String validatedText = validate(text);
			db.setTextBingoSquare(validSeason, id, validatedText);;
			return "redirect:/bingo/admin/" + year + "#" + id;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/mark")
	@Transactional
	public String markBingoSquare(@PathVariable("year") int year, 
		@RequestParam("id") int id) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			db.toogleMarkBingoSquare(validSeason, id);
			return "redirect:/bingo/admin/" + year + "#" + id;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	private String validate(String text) {
		text = text.strip();
		text = text.replaceAll(REGEX, "");
		return text;
	}
}
