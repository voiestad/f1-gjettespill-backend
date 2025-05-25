package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.response.LinkListResponse;
import no.vebb.f1.util.response.TablesResponse;

/**
 * Class is responsible for displaying stats about users guesses.
 */
@RestController
@RequestMapping("/api/public/user")
public class ProfileController {

	@Autowired
	private UserService userService;

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	/**
	 * Handles GET request for /user.
	 * 
	 * @return links to all users
	 */
	@GetMapping
	public ResponseEntity<LinkListResponse> users() {
		LinkListResponse res = new LinkListResponse();
		String title = "Brukere";
		res.title = title;
		res.heading = title;
		Map<String, String> linkMap = new LinkedHashMap<>();
		List<User> users = db.getAllUsers();
		for (User user : users) {
			linkMap.put(user.username, "/user/" + user.id);
		}
		res.links = linkMap;
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	/**
	 * Handles GET request for /user/{id} where id is the UUID of the guesser.
	 * If user does not exist, redirects to /. If users are still able to guess
	 * before season starts, the page will be blank if the page does not belongs to
	 * the logged in user. Else it will display stats from the users guesses.
	 * 
	 * @param id    of user
	 * @return guessing profile
	 */
	@GetMapping("/{id}")
	public ResponseEntity<TablesResponse> guesserProfile(@PathVariable("id") UUID id) {
		Optional<User> optUser = userService.loadUser(id);
		if (optUser.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		User user = optUser.get();
		return getGuesserProfile(user);
	}

	/**
	 * Handles GET request for /user/myprofile. Displays an overview of the
	 * guesses of the user. If the user is not logged in, the user will be
	 * redirected to /.
	 */
	@GetMapping("/myprofile")
	public ResponseEntity<TablesResponse> myProfile() {
		Optional<User> optUser = userService.loadUser();
		if (optUser.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		User user = optUser.get();
		return getGuesserProfile(user);
	}

	private ResponseEntity<TablesResponse> getGuesserProfile(User user) {
		TablesResponse res = new TablesResponse();
		if (!cutoff.isAbleToGuessCurrentYear() || userService.isLoggedInUser(user)) {
			try {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				UserScore userScore = new UserScore(user.id, year, db);
				res.tables = userScore.getAllTables();
			} catch (InvalidYearException e) {
			}
		} else {
			String title = "Tippingen er tilgjenglig snart!";
			Table table = new Table(title, Arrays.asList(), Arrays.asList());
			List<Table> tables = Arrays.asList(table);
			res.tables = tables;
		}

		res.title =  user.username;
		res.heading =  user.username;
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping("/compare")
	public ResponseEntity<TablesResponse> compareUsers(
			@RequestParam(value = "year", required = false) Integer year,
			@RequestParam(value = "user1", required = false) UUID userId1,
			@RequestParam(value = "user2", required = false) UUID userId2) {
		TablesResponse res = new TablesResponse();
		Optional<User> optUser1 = userService.loadUser(userId1);
		Optional<User> optUser2 = userService.loadUser(userId2);
		List<Table> tables = new ArrayList<>();
		String title = "Sammenlign brukere";
		res.title = title;
		res.heading = title;
		res.tables = tables;
		
		if (optUser1.isEmpty() || optUser2.isEmpty() || year == null) {
			return new ResponseEntity<>(res, HttpStatus.OK);
		}
		try {
			Year seasonYear = new Year(year, db);
			if (cutoff.isAbleToGuessYear(seasonYear)) {
				String tableTitle = "Tippingen er tilgjenglig snart!";
				tables.add(new Table(tableTitle, Arrays.asList(), Arrays.asList()));
				return new ResponseEntity<>(res, HttpStatus.OK);
			}
			User user1 = optUser1.get();
			User user2 = optUser2.get();
			UserScore userScore1 = new UserScore(user1.id, seasonYear, db);
			UserScore userScore2 = new UserScore(user2.id, seasonYear, db);
			tables.addAll(userScore1.comparisonTables(userScore2));
			title = String.format("%s vs %s", user1.username, user2.username);
			res.title = title;
			res.heading = title;
		} catch (InvalidYearException e) {
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping("/list")
	public ResponseEntity<List<PublicUser>> listUsers() {
		List<PublicUser> res = db.getAllUsers().stream()
			.map(user -> new PublicUser(user))
			.toList();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
