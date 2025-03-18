package no.vebb.f1.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	
	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String displayBingocard(Model model) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			model.addAttribute("bingoCard", db.getBingoCard(year));
		} catch (InvalidYearException e) {
		}
		return "bingo";
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
		return "linkList";
	}

	@GetMapping("/admin/{year}")
	public String administrateBingoCard(@PathVariable("year") int year, Model model) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			model.addAttribute("bingoCard", db.getBingoCard(validSeason));
			return "bingoCardAdmin";
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/add")
	public String addBingoSquare(@PathVariable("year") int year,
		@RequestParam("text") String text) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			// TODO: Validate user inputted text
			Year validSeason = new Year(year, db);
			// TODO: Set ID
			int id = 0;
			BingoSquare bingoSquare = new BingoSquare(text, false, id, validSeason);
			db.addBingoSquare(bingoSquare);
			return "redirect:/bingo/admin/" + year;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/add")
	public String updateBingoSquareText(@PathVariable("year") int year,
		@RequestParam("id") int id, @RequestParam("text") String text) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			// TODO: Validate user inputted text and ID
			Year validSeason = new Year(year, db);
			BingoSquare bingoSquare = new BingoSquare(text, false, id, validSeason);
			db.addBingoSquare(bingoSquare);
			return "redirect:/bingo/admin/" + year;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}

	@PostMapping("/admin/{year}/mark")
	public String markBingoSquare(@PathVariable("year") int year, 
		@RequestParam("id") int id) {
		if (!userService.isBingomaster()) {
			return "redirect:/bingo";
		}
		try {
			Year validSeason = new Year(year, db);
			db.toogleMarkBingoSquare(validSeason, id);
			return "redirect:/bingo/admin/" + year;
		} catch (InvalidYearException e) {
			return "redirect:/bingo/admin";
		}
	}
}
