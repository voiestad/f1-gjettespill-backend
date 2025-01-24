package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.NoAvailableRaceException;
import no.vebb.f1.util.Table;
import no.vebb.f1.util.TimeUtil;

import org.springframework.ui.Model;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	@GetMapping("/")
	public String home(Model model) {
		int year = TimeUtil.getCurrentYear();
		boolean loggedOut = !userService.isLoggedIn();
		model.addAttribute("loggedOut", loggedOut);
		Table leaderBoard = getLeaderBoard();
		model.addAttribute("leaderBoard", leaderBoard);
		if (leaderBoard.getHeader().size() == 0) {
			List<String> guessers = db.getSeasonGuessers(year);
			model.addAttribute("guessers", guessers);
			model.addAttribute("guessersNames", new String[0]);
			model.addAttribute("scores", new int[0]);
		} else {
			setGraph(model);
		}
		model.addAttribute("raceGuess", isRaceGuess());

		model.addAttribute("isAdmin", userService.isAdmin());
		return "public";
	}

	@GetMapping("/contact")
	public String contact() {
		return "contact";
	}

	private boolean isRaceGuess() {
		int year = TimeUtil.getCurrentYear();
		try {
			int raceId = db.getLatestRaceForPlaceGuess(year).id;
			return !cutoff.isAbleToGuessRace(raceId);
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (NoAvailableRaceException e) {
			return false;
		}
	}

	private Table getLeaderBoard() {
		List<String> header = Arrays.asList("Plass", "Navn", "Poeng");
		List<List<String>> body = new ArrayList<>();
		if (cutoff.isAbleToGuessCurrentYear()) {
			return new Table("Sesongen starter snart", new ArrayList<>(), new ArrayList<>());
		}
		int year = TimeUtil.getCurrentYear();
		List<Guesser> leaderBoard = db.getAllUsers().stream()
			.map(id -> {
				UserScore userScore = new UserScore(id, year, db);
				User user = userService.loadUser(id).get();
				return new Guesser(user.username, userScore.getScore(), id);
			})
			.filter(guesser -> guesser.points > 0)
			.sorted()
			.toList();

		for (int i = 0; i < leaderBoard.size(); i++) {
			Guesser guesser = leaderBoard.get(i);
			body.add(Arrays.asList(
				String.valueOf(i+1),
				guesser.username,
				String.valueOf(guesser.points),
				guesser.id.toString()
			));
		}
		return new Table("Rangering", header, body);
	}

	private List<Integer> getSeasonRaceIds() {
		List<Integer> raceIds = new ArrayList<>();
		raceIds.add(-1);
		int year = TimeUtil.getCurrentYear();
		db.getRaceIdsFinished(year).forEach(id -> raceIds.add(id));

		return raceIds;
	}

	private void setGraph(Model model) {
		int year = TimeUtil.getCurrentYear();
		List<UUID> guessers = db.getSeasonGuesserIds(year);
		List<String> guessersNames = db.getSeasonGuessers(year);
		model.addAttribute("guessersNames", guessersNames);
		List<Integer> raceIds = getSeasonRaceIds();
		List<List<Integer>> scores = new ArrayList<>();
		for (UUID id : guessers) {
			scores.add(
				raceIds.stream()
					.map(raceId -> new UserScore(id, year, raceId, db).getScore())
					.toList()
			);
		}
		model.addAttribute("scores", scores);
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
