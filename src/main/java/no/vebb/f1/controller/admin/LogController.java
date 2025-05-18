package no.vebb.f1.controller.admin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

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
		linkMap.put("Info", "/admin/log/info");
		linkMap.put("Importer", "/admin/log/importer");
		linkMap.put("Error", "/admin/log/error");
		linkMap.put("Cache", "/admin/log/cache");
		return "util/linkList";
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
		Collections.sort(files, Collections.reverseOrder());
		for (String file : files) {
			linkMap.put(file.substring(0, 10),String.format("/admin/log/%s/%s", type, file));
		}
		return "util/linkList";
	}
	
	@GetMapping("/{type}/{logFile}")
	public String chooseInfoDate(Model model, @PathVariable("type") String type,
		@PathVariable("logFile") String logFile) {
		if (!isValidFile(type, logFile)) {
			return "redirect:/admin/log/" + type;
		}
		model.addAttribute("title", String.format("%s log: %s", 
			type.substring(0, 1).toUpperCase() + type.substring(1),
			logFile.substring(0, 10)));
		File file = new File(String.format("%s/%s/%s", logPath, type, logFile));
		try {
			Scanner scanner = new Scanner(file);
			Stack<StringBuffer> stack = new Stack<>();
			StringBuffer section = new StringBuffer();
			while (scanner.hasNextLine()) {
				String logLine = scanner.nextLine();
				if (isLogLineStart(logLine)) {
					stack.add(section);
					section = new StringBuffer();
				}
				section.append(logLine).append('\n');
			}
			stack.add(section);
			StringBuffer buffer = new StringBuffer();
			while (!stack.empty()) {
				buffer.append(stack.pop());
			}
			model.addAttribute("text", buffer.toString());
			scanner.close();
		} catch (IOException e) {	
			return "redirect:/admin/log/" + type;
		}
		return "admin/log";
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
		return type.matches("error|info|cache|importer");
	}

	private boolean isLogLineStart(String logLine) {
		try {
			String time = logLine.substring(0, 20);
			LocalDateTime.parse(time);
		} catch (DateTimeParseException e) {
			return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

}
