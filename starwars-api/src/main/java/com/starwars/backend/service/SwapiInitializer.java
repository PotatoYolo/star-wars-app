package com.starwars.backend.service;

import com.starwars.backend.integration.SwapiSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class SwapiInitializer {

    private final SwapiSyncService swapiSyncService;

    @PostConstruct
    public void init() {
        swapiSyncService.syncAllData();
    }
}
