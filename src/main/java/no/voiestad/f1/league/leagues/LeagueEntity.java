package no.voiestad.f1.league.leagues;

import java.util.UUID;

import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

@Entity
@Table(name = "leagues")
public class LeagueEntity {
    @Id
    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    @Column(name = "league_name", nullable = false, columnDefinition = "citext")
    private String leagueName;

    @Embedded
    private Year year;

    protected LeagueEntity() {}

    public LeagueEntity(UUID leagueId, String leagueName, Year year) {
        this.leagueId = leagueId;
        this.leagueName = leagueName;
        this.year = year;
    }

    public UUID leagueId() {
        return leagueId;
    }

    public String leagueName() {
        return leagueName;
    }

    public Year year() {
        return year;
    }

    public LeagueEntity withLeagueName(String leagueName) {
        return new LeagueEntity(leagueId, leagueName, year);
    }
}
