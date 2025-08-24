package no.vebb.f1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRespository extends JpaRepository<User, UUID> {

    Optional<User> findByGoogleId(String googleId);

    @Query(value = "SELECT * FROM users WHERE username = :username :: citext", nativeQuery = true)
    Optional<User> findByUsername(String username);

    List<User> findAllByOrderByUsername();

    @Modifying
    @Query(value = """
            UPDATE users
            SET username = 'Anonym' || nextVal('anonymous_username_seq'),
                google_id = :userId
            WHERE user_id = :userId
            """, nativeQuery = true)

    void anonymizeUser(UUID userId);
}
