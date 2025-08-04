package com.starwars.backend.scheduler;

import com.starwars.backend.integration.SwapiSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SwapiScheduler {

    private final SwapiSyncService swapiSyncService;

    @Scheduled(cron = "0 0 4 * * *")
    public void scheduledSync() {
        log.info("Scheduled SWAPI sync started at 04:00 AM");

        try {
            swapiSyncService.syncAllData();
            log.info("Scheduled SWAPI sync completed successfully");
        } catch (Exception e) {
            log.error("Scheduled SWAPI sync failed: {}", e.getMessage(), e);
        }
    }
}
