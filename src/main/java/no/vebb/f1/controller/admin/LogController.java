package no.vebb.f1.controller.admin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/log")
public class LogController {
	
	private final String logPath = "logs";

	@GetMapping("/list")
	public ResponseEntity<List<String>> listDates(@RequestParam("type") String type) {
		if (isInvalidLogType(type)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		List<String> files = getFilesInFolder(type).stream()
				.map(file -> file.substring(0, 10))
				.sorted(Collections.reverseOrder())
				.toList();
		return new ResponseEntity<>(files, HttpStatus.OK);
	}
	
	@GetMapping("/file")
	public ResponseEntity<String> getLogfileContent(@RequestParam("type") String type,
									@RequestParam("date") String date) {
		if (!isValidFile(type, date)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		File file = new File(String.format("%s/%s/%s", logPath, type, date + ".log"));
		try {
			Scanner scanner = new Scanner(file);
			Stack<StringBuilder> stack = new Stack<>();
			StringBuilder section = new StringBuilder();
			while (scanner.hasNextLine()) {
				String logLine = scanner.nextLine();
				if (isLogLineStart(logLine)) {
					stack.add(section);
					section = new StringBuilder();
				}
				section.append(logLine).append('\n');
			}
			stack.add(section);
			StringBuilder buffer = new StringBuilder();
			while (!stack.empty()) {
				buffer.append(stack.pop());
			}
			scanner.close();
			return new ResponseEntity<>(buffer.toString(), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
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

	private boolean isValidFile(String type, String date) {
		if (isInvalidLogType(type)) {
			return false;
		}
		List<String> files = getFilesInFolder(type);
		return files.contains(date + ".log");
	}

	private boolean isInvalidLogType(String type) {
		return !type.matches("error|info|cache|importer");
	}

	private boolean isLogLineStart(String logLine) {
		try {
			String time = logLine.substring(0, 20);
			LocalDateTime.parse(time);
		} catch (DateTimeParseException | IndexOutOfBoundsException e) {
			return false;
		}
        return true;
	}

}
