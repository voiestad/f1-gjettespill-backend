package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

import org.springframework.ui.Model;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;
	
	private JdbcTemplate jdbcTemplate;
	private int year = 2024;

	public HomeController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/")
	public String home(Model model) {
		Optional<User> user = userService.loadUser();
		boolean loggedOut = !user.isPresent();
		List<List<String>> leaderBoard = getLeaderBoard();
		model.addAttribute("leaderBoard", leaderBoard);
		model.addAttribute("loggedOut", loggedOut);
		return "public";
	}

	public List<List<String>> getLeaderBoard() {
		List<List<String>> leaderBoard = new ArrayList<>();
		leaderBoard.add(Arrays.asList("Plass", "Navn", "Poeng"));
		List<Guesser> leaderBoardUnsorted = new ArrayList<>();
		
		final String getAllUsersSql = "SELECT id FROM User";
		List<UUID> userIds = jdbcTemplate.query(getAllUsersSql, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
		for (UUID id : userIds) {
			User user = userService.loadUser(id).get();
			UserScore userScore = new UserScore(user, year, jdbcTemplate);
			leaderBoardUnsorted.add(new Guesser(user.username, userScore.getScore(), user.id));
		}
		
		Collections.sort(leaderBoardUnsorted);
		for (int i = 0; i < leaderBoardUnsorted.size(); i++) {
			Guesser guesser = leaderBoardUnsorted.get(i);
			leaderBoard.add(Arrays.asList(String.valueOf(i+1), guesser.username, String.valueOf(guesser.points), guesser.id.toString()));
		}
		return leaderBoard;
	}

	class Guesser implements Comparable<Guesser> {

		public final String username;
		public final int points;
		public final UUID id;

		public Guesser(String username, int points, UUID id) {
			this.username = username;
			this.points = points;
			this.id = id;
		}

		@Override
		public int compareTo(Guesser other) {
			if (points < other.points) {
				return 1;
			} else if (points > other.points) {
				return -1;
			}
			return 0;
		}
	}
}
