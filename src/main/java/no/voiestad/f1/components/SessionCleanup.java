package no.voiestad.f1.components;

import no.voiestad.f1.util.TimeUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class SessionCleanup {

    private static final long MAX_IDLE_MILLIS = Duration.ofHours(1).toMillis();

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final JdbcTemplate jdbcTemplate;

    public SessionCleanup(FindByIndexNameSessionRepository<? extends Session> sessionRepository, JdbcTemplate jdbcTemplate) {
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = TimeUtil.HALF_HOUR, initialDelay = TimeUtil.SECOND * 3)
    public void removeUnregisteredSessions() {
        getUnregisteredSessionIds().forEach(sessionRepository::deleteById);
    }

    public List<String> getUnregisteredSessionIds() {
        final String sql = """
            SELECT s.SESSION_ID
            FROM SPRING_SESSION s
            WHERE 
                s.LAST_ACCESS_TIME < ?
                AND (
                    s.PRINCIPAL_NAME IS NULL
                    OR NOT EXISTS (
                        SELECT 1
                        FROM user_providers up
                        WHERE up.provider_id = s.PRINCIPAL_NAME
                    )
                )
        """;
        long cutoff = System.currentTimeMillis() - MAX_IDLE_MILLIS;
        return jdbcTemplate.queryForList(sql, String.class, cutoff);
    }
}
