package no.vebb.f1.controller.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;

	@GetMapping
	public String adminHome(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Admin Portal");
		Map<String, String> linkMap = new LinkedHashMap<>();
		linkMap.put("Registrer flagg", "/admin/flag");
		linkMap.put("Administrer sesonger", "/admin/season");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}
}
