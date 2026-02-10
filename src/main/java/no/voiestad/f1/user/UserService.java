package no.voiestad.f1.user;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import no.voiestad.f1.bingo.BingoService;
import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.notification.NotificationService;
import no.voiestad.f1.user.admin.AdminRepository;
import no.voiestad.f1.user.domain.Username;
import no.voiestad.f1.exception.NoUsernameException;
import no.voiestad.f1.exception.NotAdminException;

import no.voiestad.f1.util.TimeUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    private final UserRespository userRespository;
    private final AdminRepository adminRepository;
    private final NotificationService notificationService;
    private final BingoService bingoService;
    private final LeagueService leagueService;
    private final UserProviderRepository userProviderRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserLinkingRepository userLinkingRepository;

    public UserService(UserRespository userRespository, AdminRepository adminRepository, NotificationService notificationService, BingoService bingoService, LeagueService leagueService, UserProviderRepository userProviderRepository, OAuth2AuthorizedClientService authorizedClientService, UserLinkingRepository userLinkingRepository) {
        this.userRespository = userRespository;
        this.adminRepository = adminRepository;
        this.notificationService = notificationService;
        this.bingoService = bingoService;
        this.leagueService = leagueService;
        this.userProviderRepository = userProviderRepository;
        this.authorizedClientService = authorizedClientService;
        this.userLinkingRepository = userLinkingRepository;
    }

    public Optional<UserEntity> loadUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            return Optional.empty();
        }

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        if (client == null) {
            return Optional.empty();
        }
        final String providerId = client.getPrincipalName();
        final String provider = client.getClientRegistration().getRegistrationId();
        return userRespository.findByProviderId(providerId, provider);
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

    public Map<String, String> getProviders() throws NoUsernameException {
        Optional<UserEntity> user = loadUser();
        if (user.isEmpty()) {
            throw new NoUsernameException("User does not have a username, which is required for this end point");
        }
        List<UserProviderEntity> userProviderEntities = userProviderRepository.findAllByUserId(user.get().id());
        Map<String, String> providers = new LinkedHashMap<>();
        for (UserProviderEntity userProviderEntity : userProviderEntities) {
            providers.put(userProviderEntity.provider(), userProviderEntity.providerId());
        }
        return providers;
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

    public void addUser(Username username, OAuth2AuthorizedClient client) {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, username.username);
        userRespository.save(userEntity);
        final String providerId = client.getPrincipalName();
        final String provider = client.getClientRegistration().getRegistrationId();
        int id = userProviderRepository.getNextId();
        UserProviderEntity userProviderEntity = new UserProviderEntity(id, userId, providerId, provider);
        userProviderRepository.save(userProviderEntity);
    }

    public boolean addProvider(String linkCode, OAuth2AuthorizedClient client) {
        Optional<UserLinkingEntity> optUserLinking = userLinkingRepository.findByCode(linkCode);
        if (optUserLinking.isEmpty()) {
            return false;
        }
        UserLinkingEntity userLinking = optUserLinking.get();
        if (Instant.now().compareTo(userLinking.validTo()) >= 0) {
            return false;
        }
        final String provider = client.getClientRegistration().getRegistrationId();
        final String providerId = client.getPrincipalName();
        if (userProviderRepository.existsByProviderAndProviderId(provider, providerId)) {
            return false;
        }
        final UUID userId = userLinking.userId();
        int id = userProviderRepository.getNextId();
        UserProviderEntity userProviderEntity = new UserProviderEntity(id, userId, providerId, provider);
        userProviderRepository.save(userProviderEntity);
        userLinkingRepository.deleteByUserId(userId);
        return true;
    }

    public void deleteProvider(String provider) {
        UUID userId = getUser().id();
        userProviderRepository.deleteByUserIdAndProvider(userId, provider);
    }

    public String addUserLinking(UUID userId) {
        userLinkingRepository.deleteByUserId(userId);
        int id = userLinkingRepository.getNextId();
        byte[] bytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        HexFormat hex = HexFormat.of();
        String code = hex.formatHex(bytes);
        UserLinkingEntity userLinking = new UserLinkingEntity(id, userId, code, Instant.now().plus(Duration.ofMinutes(5)));
        userLinkingRepository.save(userLinking);
        return code;
    }

    @Scheduled(fixedRate = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
    @Transactional
    public void removeExpiredLinkCodes() {
        userLinkingRepository.deleteExpiredCodes(Instant.now());
    }

    public void changeUsername(Username newUsername) {
        UserEntity userEntity = getUser();
        userEntity.setUsername(newUsername.username);
        userRespository.save(userEntity);
    }

    public void deleteUser() {
        UUID userId = getUser().id();
        userProviderRepository.deleteAllByUserId(userId);
        userRespository.anonymizeUser(userId);
        notificationService.clearUserFromNtfy(userId);
        bingoService.removeBingomaster(userId);
        leagueService.clearSentInvitations(userId);
    }

}
