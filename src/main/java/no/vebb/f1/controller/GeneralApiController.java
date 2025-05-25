package no.vebb.f1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;

@RestController
@RequestMapping("/api/public")
public class GeneralApiController {

	@Autowired
	private Database db;
	
	@GetMapping("/year/list")
	public ResponseEntity<List<Integer>> listYears() {
		List<Integer> res = db.getAllValidYears().stream()
			.map(year -> year.value)
			.toList();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
