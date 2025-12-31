package no.voiestad.f1.event;

import no.voiestad.f1.importing.Importer;
import no.voiestad.f1.scoring.ScoreCalculator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EventHandler {

    private final ScoreCalculator scoreCalculator;
    private final Importer importer;

    public EventHandler(ScoreCalculator scoreCalculator, Importer importer) {
        this.scoreCalculator = scoreCalculator;
        this.importer = importer;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeagueChanged(LeagueChangedEvent event) {
        scoreCalculator.calculateLeague(event.leagueId(), event.year());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCalculateScore(CalculateScoreEvent event) {
        scoreCalculator.calculateScores();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onImportData(ImportDataEvent event) {
        importer.importData();
    }
}
