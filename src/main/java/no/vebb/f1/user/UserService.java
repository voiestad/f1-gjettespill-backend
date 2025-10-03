package no.vebb.f1.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.bingo.BingoService;
import no.vebb.f1.notification.NotificationService;
import no.vebb.f1.user.admin.AdminRepository;
import no.vebb.f1.user.domain.Username;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import no.vebb.f1.exception.NoUsernameException;
import no.vebb.f1.exception.NotAdminException;

@Service
public class UserService {

    private final UserRespository userRespository;
    private final AdminRepository adminRepository;
    private final NotificationService notificationService;
    private final BingoService bingoService;

    public UserService(UserRespository userRespository, AdminRepository adminRepository, NotificationService notificationService, BingoService bingoService) {
        this.userRespository = userRespository;
        this.adminRepository = adminRepository;
        this.notificationService = notificationService;
        this.bingoService = bingoService;
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
        UserEntity userEntity = getUser();
        userRespository.anonymizeUser(userEntity.id());
        notificationService.clearUserFromNtfy(userEntity.id());
        bingoService.removeBingomaster(userEntity.id());
    }

}
