package no.vebb.f1.scoring;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.scoring.userTables.Summary;

import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.year.Year;

public class UserScoreResponse {

	public final UserScore userScores;
	public final Summary summary;

	public UserScoreResponse(
			PublicUserDto user,
			Year year,
			RaceId raceId,
			RaceService raceService,
			PlacementService placementService,
			GuessService guessService,
			ScoreService scoreService,
			ResultService resultService
	) {
		this.userScores = new UserScore(user, year, raceId, raceService, guessService, scoreService, resultService);
		this.summary = placementService.getSummary(raceId, year, user);
	}

	public UserScoreResponse(
			PublicUserDto user,
			Year year,
			RaceService raceService,
			PlacementService placementService,
			GuessService guessService,
			ScoreService scoreService,
			ResultService resultService
	) {
		this.userScores = new UserScore(user, year, raceService, guessService, scoreService, resultService);
		this.summary = placementService.getSummary(this.userScores.raceId, year, user);
	}

}
