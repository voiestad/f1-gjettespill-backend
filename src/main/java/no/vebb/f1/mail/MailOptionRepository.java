package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MailOptionRepository extends JpaRepository<MailOption, Integer> {
    @Query("SELECT mo FROM MailOption mo ORDER BY mo.mailOption")
    List<MailOption> findAllOrderByMailOption();
}
