package no.voiestad.f1.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.bingo.BingoService;
import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.notification.NotificationService;
import no.voiestad.f1.user.admin.AdminRepository;
import no.voiestad.f1.user.domain.Username;
import no.voiestad.f1.exception.NoUsernameException;
import no.voiestad.f1.exception.NotAdminException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;



@Service
public class UserService {

    private final UserRespository userRespository;
    private final AdminRepository adminRepository;
    private final NotificationService notificationService;
    private final BingoService bingoService;
    private final LeagueService leagueService;

    public UserService(UserRespository userRespository, AdminRepository adminRepository, NotificationService notificationService, BingoService bingoService, LeagueService leagueService) {
        this.userRespository = userRespository;
        this.adminRepository = adminRepository;
        this.notificationService = notificationService;
        this.bingoService = bingoService;
        this.leagueService = leagueService;
    }

    public Optional<UserEntity> loadUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        final String googleId = authentication.getName();
        return userRespository.findByGoogleId(googleId);
    }

    public Optional<UserEntity> loadUser(UUID id) {
        return userRespository.findById(id);
    }

    public Optional<UserEntity> loadOrCurrentUser(UUID id) {
        if (id == null) {
            return loadUser();
        }
        return loadUser(id);
    }

    public boolean isLoggedIn() {
        return loadUser().isPresent();
    }

    public boolean isAdmin() {
        return loadUser().filter(user -> adminRepository.existsById(user.id())).isPresent();
    }

    public void adminCheck() throws NotAdminException {
        if (!isAdmin()) {
            throw new NotAdminException("User is not admin and does not have the required permission for this end point");
        }
    }

    public void usernameCheck() throws NoUsernameException {
        if (!isLoggedIn()) {
            throw new NoUsernameException("User does not have a username, which is required for this end point");
        }
    }

    public UserEntity getUser() throws NoUsernameException {
        Optional<UserEntity> user = loadUser();
        if (user.isEmpty()) {
            throw new NoUsernameException("User does not have a username, which is required for this end point");
        }
        return user.get();
    }

    public boolean isBingomaster() {
        Optional<UserEntity> user = loadUser();
        return user.filter(value -> bingoService.isBingomaster(value.id())).isPresent();
    }

    public boolean isLoggedInUser(UserEntity loggedInUserEntity) {
        return loadUser().map(userEntity -> userEntity.id().equals(loggedInUserEntity.id())).orElse(false);
    }

    public List<UserEntity> getAllUsers() {
        return userRespository.findAllByOrderByUsername();
    }

    public void addUser(Username username, OAuth2User principal) {
        final String googleId = principal.getName();
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), googleId, username.username);
        userRespository.save(userEntity);
    }

    public void changeUsername(Username newUsername) {
        UserEntity userEntity = getUser();
        userEntity.setUsername(newUsername.username);
        userRespository.save(userEntity);
    }

    public void deleteUser() {
        UUID userId = getUser().id();
        userRespository.anonymizeUser(userId);
        notificationService.clearUserFromNtfy(userId);
        bingoService.removeBingomaster(userId);
        leagueService.clearSentInvitations(userId);
    }

}
