package no.vebb.f1.controller.admin;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin/bingo")
public class BingomasterController {
	
	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String administrateBingomasters(Model model) {
		model.addAttribute("title", "Administrer bingomasters");
		model.addAttribute("bingomasters", db.getBingomasters());
		model.addAttribute("nonBingomasters", db.getNonBingomasters());
		return "bingomasters";
	}

	@PostMapping("/add")
	public String addBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
		if (userService.loadUser(bingomaster).isPresent()) {
			db.addBingomaster(bingomaster);
		}
		return "redirect:/admin/bingo";
	}
	
	@PostMapping("/remove")
	public String removeBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
		if (userService.loadUser(bingomaster).isPresent()) {
			db.removeBingomaster(bingomaster);
		}
		return "redirect:/admin/bingo";
	}

}
