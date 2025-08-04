package com.starwars.backend.controller;

import com.starwars.backend.integration.SwapiSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/swapi")
@Slf4j
public class SwapiController {

    private final SwapiSyncService swapiSyncService;

    @PostMapping("/resync")
    public ResponseEntity<Void> manualResync() {
        try {
            swapiSyncService.syncAllData();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("SWAPI manual resync failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}