package stubidp.stubidp.repositories.reaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.repositories.IdpSessionRepository;

import java.time.Duration;

import static java.text.MessageFormat.format;

public class StaleSessionReaper implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaleSessionReaper.class);

    /**
     * this operates on all sessions in the database, both eidas and verify
     */
    private final IdpSessionRepository verifySessionRepository;
    private final Duration sessionIsStaleAfter;

    public StaleSessionReaper(IdpSessionRepository verifySessionRepository,
                              StaleSessionReaperConfiguration staleSessionReaperConfiguration) {
        this.verifySessionRepository = verifySessionRepository;
        this.sessionIsStaleAfter = staleSessionReaperConfiguration.getSessionIsStaleAfter();
    }

    @Override
    public void run() {
        final long sessionsInDatabaseBefore = verifySessionRepository.countSessionsInDatabase();
        LOGGER.info(format("{0} active sessions before reaping (eidas + verify)", sessionsInDatabaseBefore));
        final long staleSessionsToReap = verifySessionRepository.countSessionsOlderThan(sessionIsStaleAfter);
        LOGGER.info(format("{0} session(s) (approx) are expected to be reaped (eidas + verify)", staleSessionsToReap));
        verifySessionRepository.deleteSessionsOlderThan(sessionIsStaleAfter);
        final long sessionsInDatabaseAfter = verifySessionRepository.countSessionsInDatabase();
        LOGGER.info(format("{0} active sessions after reaping (eidas + verify)", sessionsInDatabaseAfter));
    }
}
