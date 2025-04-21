package no.vebb.f1.controller.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@GetMapping
	public String adminHome(Model model) {
		model.addAttribute("title", "Admin portal");
		Map<String, String> linkMap = new LinkedHashMap<>();
		linkMap.put("Registrer flagg", "/admin/flag");
		linkMap.put("Administrer sesonger", "/admin/season");
		linkMap.put("Administrer bingomasters", "/admin/bingo");
		linkMap.put("Logg", "/admin/log");
		linkMap.put("Sikkerhetskopi", "/admin/backup");
		model.addAttribute("linkMap", linkMap);
		return "util/linkList";
	}

	@GetMapping("/backup")
	public String backupPage() {
		return "admin/backup";
	}
}
