package no.voiestad.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.league.domain.LeagueDTO;
import no.voiestad.f1.league.domain.LeagueInvitationDTO;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeagueController {
    private final LeagueService leagueService;
    private final YearService yearService;
    private final UserService userService;

    public LeagueController(
            LeagueService leagueService,
            YearService yearService,
            UserService userService) {
        this.leagueService = leagueService;
        this.yearService = yearService;
        this.userService = userService;
    }

    @GetMapping("/api/public/leagues")
    public ResponseEntity<List<LeagueDTO>> getLeagues(@RequestParam(name = "year", required = false) Integer inputYear) {
        Optional<Year> optYear = yearService.getOrCurrentYear(inputYear);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(List.of(),HttpStatus.OK);
        }
        List<LeagueDTO> leagues = leagueService.getLeagues(optYear.get()).stream()
                .map(LeagueDTO::fromEntity)
                .toList();
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @GetMapping("/api/public/league/memberships")
    public ResponseEntity<List<LeagueDTO>> getLeagueMemberships(
            @RequestParam("userId") UUID userId,
            @RequestParam(name = "year", required = false) Integer inputYear) {
        Optional<UserEntity> optUser = userService.loadUser(userId);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Year> optYear = yearService.getOrCurrentYear(inputYear);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(List.of(),HttpStatus.OK);
        }
        List<LeagueDTO> leagues = leagueService.getMemberships(optUser.get().id(), optYear.get()).stream()
                .map(LeagueDTO::fromEntity)
                .toList();
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @GetMapping("/api/public/league/members")
    public ResponseEntity<List<PublicUserDto>> getLeagueMembers(@RequestParam("leagueId") UUID leagueId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<PublicUserDto> members = leagueService.getMembers(leagueId).stream()
                .map(PublicUserDto::fromEntity)
                .toList();
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    @PutMapping("/api/league")
    public ResponseEntity<?> createLeague(@RequestParam("leagueName") String leagueName) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/league/delete")
    public ResponseEntity<?> deleteLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/rename")
    public ResponseEntity<?> updateLeague(
            @RequestParam("leagueName") String leagueName,
            @RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/leave")
    public ResponseEntity<?> leaveLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/transferOwnership")
    public ResponseEntity<?> transferOwnership(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/league/invitations/pending")
    public ResponseEntity<List<LeagueInvitationDTO>> getPendingLeagueInvitations() {
        UUID userId = userService.getUser().id();
        List<LeagueInvitationDTO> invitations = leagueService.getPendingInvitations(userId);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    @GetMapping("/api/league/invitations/sent")
    public ResponseEntity<List<LeagueInvitationDTO>> getSentLeagueInvitations() {
        UUID userId = userService.getUser().id();
        List<LeagueInvitationDTO> invitations = leagueService.getSentInvitations(userId);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    @PutMapping("/api/league/invite")
    public ResponseEntity<?> inviteToLeague(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/league/invite")
    public ResponseEntity<?> uninviteToLeague(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/league/join")
    public ResponseEntity<?> joinLeague(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/league/reject")
    public ResponseEntity<?> rejectLeagueInvitation(@RequestParam("leagueId") UUID leagueId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
