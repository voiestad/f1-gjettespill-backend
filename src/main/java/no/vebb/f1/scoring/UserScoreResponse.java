package no.vebb.f1.scoring;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUserDto;

public class UserScoreResponse {

	public final UserScore userScores;
	public final Summary summary;

	public UserScoreResponse(PublicUserDto user, Year year, RaceId raceId, Database db, RaceService raceService) {
		this.userScores = new UserScore(user, year, raceId, db, raceService);
		this.summary = db.getSummary(raceId, year, user);
	}

	public UserScoreResponse(PublicUserDto user, Year year, Database db, RaceService raceService) {
		this.userScores = new UserScore(user, year, db, raceService);
		this.summary = db.getSummary(this.userScores.raceId, year, user);
	}

}
