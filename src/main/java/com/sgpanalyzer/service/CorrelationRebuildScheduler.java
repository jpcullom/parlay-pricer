package com.sgpanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CorrelationRebuildScheduler {

    private final DataIngestionService dataIngestionService;
    private final CorrelationService correlationService;

    /**
     * Nightly job that:
     * 1. Pulls latest player game log data from external APIs
     * 2. Recomputes all pairwise correlations
     * 3. Updates the correlation cache
     *
     * Runs at 3:00 AM daily (configured in application.yml).
     */
    @Scheduled(cron = "${sgp.correlation.rebuild-cron}")
    public void rebuildCorrelationsNightly() {
        log.info("Nightly correlation rebuild started");

        try {
            // TODO: Step 1 — Determine current NFL season dynamically
            //  int currentSeason = Year.now().getValue();
            //  Adjust for NFL season spanning two calendar years (Sep-Feb)
            int currentSeason = 2025; // TODO: compute dynamically

            // TODO: Step 2 — Ingest latest data for all teams (currently single-team)
            //  For full automation, loop over all 32 NFL teams or maintain a configured list
            dataIngestionService.ingestPlayerStats("ATL", currentSeason);

            // TODO: Step 3 — Rebuild correlations from updated game logs
            correlationService.rebuildCorrelations();

            log.info("Nightly correlation rebuild completed successfully");
        } catch (Exception e) {
            // TODO: Add alerting (e.g., Slack webhook, email) on failure
            log.error("Nightly correlation rebuild failed", e);
        }
    }
}
