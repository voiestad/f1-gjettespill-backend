package no.vebb.f1.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/log")
public class LogController {
	
	private final String logPath = "logs";

	@GetMapping
	public String chooseLogCategory(Model model) {
		model.addAttribute("title", "Logging");
		Map<String, String> linkMap = new LinkedHashMap<>();
		model.addAttribute("linkMap", linkMap);
		linkMap.put("Error", "/admin/log/error");
		linkMap.put("Info", "/admin/log/info");
		return "linkList";
	}

	@GetMapping("/{type}")
	public String chooseDate(Model model, @PathVariable("type") String type) {
		model.addAttribute("title", "Logging");
		Map<String, String> linkMap = new LinkedHashMap<>();
		model.addAttribute("linkMap", linkMap);
		if (!isValidLogType(type)) {
			return "redirect:/admin/log";
		}
		List<String> files = getFilesInFolder(type);
		for (String file : files) {
			linkMap.put(
				String.format(
					"%s-%s-%s", 
					file.substring(6, 8),
					file.substring(4, 6),
					file.substring(0, 4)
					),
				String.format("/admin/log/%s/%s", type, file));
		}
		return "linkList";
	}
	
	@GetMapping("/{type}/{logFile}")
	public String chooseInfoDate(Model model, @PathVariable("type") String type,
		@PathVariable("logFile") String logFile) {
		if (!isValidFile(type, logFile)) {
			return "redirect:/admin/log/" + type;
		}
		model.addAttribute("title", "Logging");
		File file = new File(String.format("%s/%s/%s", logPath, type, logFile));
		try {
			Scanner scanner = new Scanner(file);
			StringBuffer buffer = new StringBuffer();
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine()).append('\n');
			}
			model.addAttribute("text", buffer.toString());
			scanner.close();
		} catch (IOException e) {	
			return "redirect:/admin/log/" + type;
		}
		return "text";
	}

	private List<String> getFilesInFolder(String folderName) {
		List<String> fileNames = new ArrayList<>();
		try {
			File folder = new File(String.format("%s/%s", logPath, folderName));
			if (!folder.exists()) {
				throw new IOException("Folder not found");
			}
			for (File file : folder.listFiles()) {
				fileNames.add(file.getName());
			}
		} catch (IOException e) {
		}
		return fileNames;
	}

	private boolean isValidFile(String type, String file) {
		if (!isValidLogType(type)) {
			return false;
		}
		List<String> files = getFilesInFolder(type);
		return files.contains(file);
	}

	private boolean isValidLogType(String type) {
		return type.matches("error|info");
	}

}
