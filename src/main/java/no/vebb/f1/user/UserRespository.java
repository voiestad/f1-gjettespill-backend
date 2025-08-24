package no.vebb.f1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRespository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.googleId = :googleId")
    Optional<User> findByGoogleId(String googleId);
    @NonNull
    Optional<User> findById(UUID userId);
    @NonNull
    List<User> findAll();
}
