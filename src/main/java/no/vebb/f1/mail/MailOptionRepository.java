package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MailOptionRepository extends JpaRepository<MailOptionEntity, Integer> {
    @Query("SELECT mo FROM MailOptionEntity mo ORDER BY mo.mailOption")
    List<MailOptionEntity> findAllOrderByMailOption();
}
