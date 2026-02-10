package no.voiestad.f1.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRespository extends JpaRepository<UserEntity, UUID> {

    @Query("""
            SELECT u FROM UserProviderEntity up
            JOIN UserEntity u ON u.id = up.userId
            WHERE up.provider = :provider
            AND up.providerId = :providerId
            """)
    Optional<UserEntity> findByProviderId(String providerId, String provider);

    @Query(value = "SELECT * FROM users WHERE username = :username :: citext", nativeQuery = true)
    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findAllByOrderByUsername();

    @Modifying
    @Query(value = """
            UPDATE users
            SET username = 'Anonym' || nextVal('anonymous_username_seq')
            WHERE user_id = :userId
            """, nativeQuery = true)
    void anonymizeUser(UUID userId);

    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE EXISTS (
                SELECT 1
                FROM FlagGuessEntity fg
                WHERE fg.id.userId = u.id
                  AND fg.id.year = :year
            )
              AND EXISTS (
                SELECT 1
                FROM DriverGuessEntity dg
                WHERE dg.id.userId = u.id
                  AND dg.id.year = :year
            )
              AND EXISTS (
                SELECT 1
                FROM ConstructorGuessEntity cg
                WHERE cg.id.userId = u.id
                  AND cg.id.year = :year
            )
            ORDER BY u.username
            """)
    List<UserEntity> findAllByGuessedYear(Year year);

}
