package no.vebb.f1.controller.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackupController {

	@GetMapping("/api/admin/getbackup")
	public ResponseEntity<Resource> getBackup() {
		try {
			String backupFileName = getMostCurrentBackup();
			File file = new File("backup", backupFileName);
			if (!file.exists()) {
				throw new IOException("Backup file not found");
			}

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + backupFileName);
			InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

			return ResponseEntity.ok()
				.headers(headers)
				.contentLength(file.length())
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
		} catch (IOException | NoSuchElementException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body(null);
		}
	}

	private String getMostCurrentBackup() throws IOException, NoSuchElementException {
		List<String> backups = new ArrayList<>();
		File folder = new File("backup");
		if (!folder.exists()) {
			throw new IOException("Folder not found");
		}
		for (File file : folder.listFiles()) {
			backups.add(file.getName());
		}
		return Collections.max(backups);
	}
}
