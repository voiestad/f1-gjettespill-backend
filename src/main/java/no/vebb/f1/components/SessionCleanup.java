package no.vebb.f1.components;

import no.vebb.f1.util.TimeUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SessionCleanup {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final JdbcTemplate jdbcTemplate;

    public SessionCleanup(FindByIndexNameSessionRepository<? extends Session> sessionRepository, JdbcTemplate jdbcTemplate) {
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = TimeUtil.HALF_HOUR, initialDelay = TimeUtil.SECOND * 3)
    public void removeUnregisteredSessions() {
        List<String> unregisteredUsers = getUnregisteredUsers();
        unregisteredUsers.forEach(sessionRepository::deleteById);
    }

    public List<String> getUnregisteredUsers() {
        final String sql = """
                	SELECT SESSION_ID, LAST_ACCESS_TIME
                	FROM SPRING_SESSION
                	WHERE PRINCIPAL_NAME NOT IN (
                		SELECT google_id from users
                	);
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);
        return sqlRes.stream()
                .filter(session -> {
                    long lastAccess = (long) session.get("LAST_ACCESS_TIME");
                    long now = System.currentTimeMillis();
                    long diff = now - lastAccess;
                    long hourMillis = 3600000;
                    return diff >= hourMillis;
                })
                .map(session -> (String) session.get("SESSION_ID"))
                .toList();
    }
}
