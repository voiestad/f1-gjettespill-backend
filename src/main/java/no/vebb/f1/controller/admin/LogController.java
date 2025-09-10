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
import java.time.LocalDate;

import no.vebb.f1.util.IOUtil;
import no.vebb.f1.exception.InvalidLogTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/log")
public class LogController {

    private final String logPath = "logs";

    @GetMapping("/list")
    public ResponseEntity<List<String>> listDates(@RequestParam("type") String type) {
        try {
            LogType logType = new LogType(type);
            List<String> files = getFileNamesInFolder(logType).stream()
                    .map(file -> file.substring(0, 10))
                    .sorted(Collections.reverseOrder())
                    .toList();
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (InvalidLogTypeException ignored) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/file")
    public ResponseEntity<String> getLogfileContent(
            @RequestParam("type") String type,
            @RequestParam("date") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LogType logType = new LogType(type);
            List<String> files = getFileNamesInFolder(logType);
            String dateFileName = localDate + ".log";
            if (!files.contains(dateFileName)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String fileName = files.get(files.indexOf(dateFileName));
            File file = new File(String.format("%s/%s/%s", logPath, logType.type(), fileName));
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
        } catch (DateTimeParseException | InvalidLogTypeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private List<String> getFileNamesInFolder(LogType logType) {
        List<String> fileNames = new ArrayList<>();
        try {
            File folder = new File(String.format("%s/%s", logPath, logType.type));
            IOUtil.getFileNamesInFolder(fileNames, folder);
        } catch (IOException ignored) {
        }
        return fileNames;
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

    private record LogType(String type) {
        private LogType {
            if (type == null || isInvalidLogType(type)) {
                throw new InvalidLogTypeException("Type is not a valid log type");
            }
        }

        private boolean isInvalidLogType(String type) {
            return !type.matches("error|info|cache|importer");
        }
    }


}
