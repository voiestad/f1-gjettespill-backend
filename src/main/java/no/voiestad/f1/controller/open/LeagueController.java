package no.voiestad.f1.controller.open;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.league.domain.LeagueDTO;
import no.voiestad.f1.league.domain.LeagueInvitationDTO;
import no.voiestad.f1.league.domain.LeagueRole;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

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
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        List<LeagueDTO> leagues = leagueService.getLeagues(optYear.get()).stream()
                .map(LeagueDTO::fromEntity)
                .toList();
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @GetMapping("/api/public/league/memberships")
    public ResponseEntity<List<LeagueDTO>> getLeagueMemberships(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(name = "year", required = false) Integer inputYear) {
        Optional<UserEntity> optUser = userService.loadOrCurrentUser(userId);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Year> optYear = yearService.getOrCurrentYear(inputYear);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
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

    @Transactional
    @PutMapping("/api/league")
    public ResponseEntity<String> createLeague(@RequestParam("leagueName") String inputLeagueName) {
        UserEntity user = userService.getUser();
        String leagueName = inputLeagueName.strip();
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året har ikke startet enda, vennligst prøv igjen senere",
                    HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Året er over, vennligst prøv igjen når neste år begynner.",
                    HttpStatus.FORBIDDEN);
        }
        if (!leagueService.isAllowedToOwnMoreLeagues(user.id(), year)) {
            return new ResponseEntity<>("Du har nådd maksimalt antall ligaeierskap på ett år.",
                    HttpStatus.FORBIDDEN);
        }
        if (!leagueService.isLeagueNameAvailable(leagueName, year)) {
            return new ResponseEntity<>("Det gitte liganavnet er allerede i bruk.", HttpStatus.CONFLICT);
        }
        if (!leagueService.hasValidLeagueNameFormat(leagueName)) {
            return new ResponseEntity<>("Det gitte liganavnet inneholder ugyldige tegn.", HttpStatus.BAD_REQUEST);
        }
        leagueService.addLeague(leagueName, year, user.id());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("/api/league/delete")
    public ResponseEntity<?> deleteLeague(@RequestParam("leagueId") UUID leagueId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UUID userId = userService.getUser().id();
        if (!leagueService.canChangeLeague(userId, leagueId)) {
            return new ResponseEntity<>("Du har ikke tillatelse til å slette ligaen.", HttpStatus.FORBIDDEN);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er ferdig og det er ikke lenger mulig å slette ligaen.", HttpStatus.FORBIDDEN);
        }
        leagueService.deleteLeague(leagueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/api/league/rename")
    public ResponseEntity<?> renameLeague(
            @RequestParam("leagueName") String inputLeagueName,
            @RequestParam("leagueId") UUID leagueId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UUID userId = userService.getUser().id();
        if (!leagueService.canChangeLeague(userId, leagueId)) {
            return new ResponseEntity<>("Du har ikke tillatelse til å endre navn på ligaen.", HttpStatus.FORBIDDEN);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er ferdig og det er ikke lenger mulig å endre navn på ligaen.", HttpStatus.FORBIDDEN);
        }
        String leagueName = inputLeagueName.strip();
        if (!leagueService.isLeagueNameAvailable(leagueName, optYear.get())) {
            return new ResponseEntity<>("Det gitte liganavnet er allerede i bruk.", HttpStatus.CONFLICT);
        }
        if (!leagueService.hasValidLeagueNameFormat(leagueName)) {
            return new ResponseEntity<>("Det gitte liganavnet inneholder ugyldige tegn.", HttpStatus.BAD_REQUEST);
        }
        if (!leagueService.renameLeague(leagueId, leagueName)) {
            return new ResponseEntity<>("Noe gikk galt.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/api/league/leave")
    public ResponseEntity<?> leaveLeague(@RequestParam("leagueId") UUID leagueId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UUID userId = userService.getUser().id();
        if (!leagueService.isMember(userId, leagueId)) {
            return new ResponseEntity<>("Du er ikke medlem av denne ligaen.", HttpStatus.FORBIDDEN);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er ferdig og det er ikke lenger mulig å forlate ligaen.", HttpStatus.FORBIDDEN);
        }
        List<UserEntity> members = leagueService.getMembers(leagueId);
        if (members.size() == 1) {
            leagueService.deleteLeague(leagueId);
            return new ResponseEntity<>("Liga slettet.", HttpStatus.OK);
        }
        if (leagueService.hasRole(userId, LeagueRole.OWNER, leagueId)) {
            for (UserEntity member : members) {
                UUID newOwner = member.id();
                if (!newOwner.equals(userId)) {
                    leagueService.transferOwnership(newOwner, leagueId);
                    break;
                }
            }
        }
        leagueService.clearInvitationsByInviterAndLeague(userId, leagueId);
        leagueService.deleteUserFromLeague(userId, leagueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/api/league/transferOwnership")
    public ResponseEntity<?> transferOwnership(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID newOwner) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UUID userId = userService.getUser().id();
        if (!leagueService.hasRole(userId, LeagueRole.OWNER, leagueId)) {
            return new ResponseEntity<>("Du er ikke eier av ligaen.", HttpStatus.FORBIDDEN);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er over og du kan ikke overføre eierskap av ligaen lenger.", HttpStatus.FORBIDDEN);
        }
        if (!leagueService.isMember(newOwner, leagueId)) {
            return new ResponseEntity<>("Du kan ikke overføre eierskap til noen som ikke er medlem av ligaen.", HttpStatus.FORBIDDEN);
        }
        leagueService.transferOwnership(newOwner, leagueId);
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

    @Transactional
    @PutMapping("/api/league/invite")
    public ResponseEntity<?> inviteToLeague(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>("Den gitte ligaen er ikke gyldig.", HttpStatus.NOT_FOUND);
        }
        Optional<UserEntity> optUser = userService.loadUser(userId);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>("Den gitte brukeren er ikke gyldig.", HttpStatus.NOT_FOUND);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er over og du kan ikke invitere noen lenger.", HttpStatus.FORBIDDEN);
        }
        UUID inviter = userService.getUser().id();
        if (!leagueService.isMember(inviter, leagueId)) {
            return new ResponseEntity<>("Du kan ikke invitere noen til ligaer du ikke selv er medlem av.", HttpStatus.FORBIDDEN);
        }
        if (leagueService.isMember(userId, leagueId)) {
            return new ResponseEntity<>("Du kan ikke invitere noen som allerede er medlem.", HttpStatus.FORBIDDEN);
        }
        boolean gotInvite = leagueService.inviteToLeague(userId, leagueId, inviter);
        if (!gotInvite) {
            return new ResponseEntity<>("Du kan ikke invitere noen flere ganger.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("/api/league/invite")
    public ResponseEntity<?> uninviteToLeague(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("userId") UUID userId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>("Den gitte ligaen er ikke gyldig.", HttpStatus.NOT_FOUND);
        }
        Optional<UserEntity> optUser = userService.loadUser(userId);
        if (optUser.isEmpty()) {
            return new ResponseEntity<>("Den gitte brukeren er ikke gyldig.", HttpStatus.NOT_FOUND);
        }
        UUID inviter = userService.getUser().id();
        boolean gotUninvited = leagueService.uninviteToLeague(userId, leagueId, inviter);
        if (!gotUninvited) {
            return new ResponseEntity<>("Du kan ikke slette en invitasjon som ikke eksisterer.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/api/league/join")
    public ResponseEntity<?> joinLeague(@RequestParam("leagueId") UUID leagueId) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Year> optYear = leagueService.getYearIfChangeable(leagueId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>("Året er over og du kan ikke bli med i ligaer lengre.", HttpStatus.FORBIDDEN);
        }
        UUID userId = userService.getUser().id();
        if (!leagueService.isInvitedToLeague(userId, leagueId)) {
            return new ResponseEntity<>("Du har ingen invitasjoner fra denne ligaen.", HttpStatus.FORBIDDEN);
        }
        leagueService.addUserToLeague(userId, leagueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("/api/league/reject")
    public ResponseEntity<?> rejectLeagueInvitation(
            @RequestParam("leagueId") UUID leagueId,
            @RequestParam("inviter") UUID inviter) {
        if (!leagueService.isValidLeagueId(leagueId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UUID userId = userService.getUser().id();
        boolean gotUninvited = leagueService.uninviteToLeague(userId, leagueId, inviter);
        if (!gotUninvited) {
            return new ResponseEntity<>("Du kan ikke slette en invitasjon som ikke eksiterer.", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
