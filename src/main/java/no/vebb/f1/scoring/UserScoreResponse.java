package no.vebb.f1.scoring;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.stats.StatsService;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.*;

import no.vebb.f1.user.PublicUserDto;

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
			StatsService statsService,
			ScoreService scoreService,
			ResultService resultService
	) {
		this.userScores = new UserScore(user, year, raceId, raceService, guessService, statsService, scoreService, resultService);
		this.summary = placementService.getSummary(raceId, year, user);
	}

	public UserScoreResponse(
			PublicUserDto user,
			Year year,
			RaceService raceService,
			PlacementService placementService,
			GuessService guessService,
			StatsService statsService,
			ScoreService scoreService,
			ResultService resultService
	) {
		this.userScores = new UserScore(user, year, raceService, guessService, statsService, scoreService, resultService);
		this.summary = placementService.getSummary(this.userScores.raceId, year, user);
	}

}
