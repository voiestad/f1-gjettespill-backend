package no.voiestad.f1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserProviderRepository extends JpaRepository<UserProviderEntity, Integer> {
    @Query(value = "SELECT NEXTVAL('user_providers_id_seq')", nativeQuery = true)
    int getNextId();

    List<UserProviderEntity> findAllByUserId(UUID userId);

    @Modifying
    void deleteAllByUserId(UUID userId);
    @Modifying
    void deleteByUserIdAndProvider(UUID userId, String provider);
    boolean existsByProviderAndProviderId(String provider, String providerId);
}
