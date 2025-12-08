package no.voiestad.f1.controller.open;

import java.util.UUID;

import no.voiestad.f1.league.LeagueService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeagueController {
    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping("/api/public/league/list/leagues")
    public ResponseEntity<?> getLeagues(@RequestParam(name = "year", required = false) Integer inputYear) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/public/league/list/memberships")
    public ResponseEntity<?> getLeagueMemberships(
            @RequestParam("userId") UUID userId,
            @RequestParam(name = "year", required = false) Integer inputYear) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/public/league/list/members")
    public ResponseEntity<?> getLeagueMembers(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/manage/add")
    public ResponseEntity<?> createLeague(@RequestParam("leagueName") String leagueName) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/manage/rename")
    public ResponseEntity<?> updateLeague(
            @RequestParam("leagueName") String leagueName,
            @RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/manage/delete")
    public ResponseEntity<?> deleteLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/manage/leave")
    public ResponseEntity<?> leaveLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/manage/transferOwnership")
    public ResponseEntity<?> transferOwnership(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/league/invitation/list")
    public ResponseEntity<?> getLeagueInvitations() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/invitation/invite")
    public ResponseEntity<?> inviteToLeague(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/invitation/join")
    public ResponseEntity<?> joinLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/invitation/reject")
    public ResponseEntity<?> rejectLeagueInvitation(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
