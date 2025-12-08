package no.voiestad.f1.controller.open;

import no.voiestad.f1.league.LeagueService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeagueController {
    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }
}
