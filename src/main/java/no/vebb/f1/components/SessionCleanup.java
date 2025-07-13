package no.vebb.f1.components;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionCleanup {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final Database db;

    public SessionCleanup(FindByIndexNameSessionRepository<? extends Session> sessionRepository, Database db) {
        this.sessionRepository = sessionRepository;
        this.db = db;
    }

    @Scheduled(fixedRate = TimeUtil.HALF_HOUR, initialDelay = TimeUtil.SECOND * 3)
    public void removeUnregisteredSessions() {
        List<String> unregisteredUsers = db.getUnregisteredUsers();
        unregisteredUsers.forEach(sessionRepository::deleteById);
    }
}
