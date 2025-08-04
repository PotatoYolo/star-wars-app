package com.starwars.backend.integration;

import com.starwars.backend.exception.SwapiSyncException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwapiSyncService {

    private final SyncPlanetService syncPlanetService;
    private final SyncCharacterService syncCharactersService;
    private final SyncFilmService syncFilmService;
    private final SyncSpeciesService syncSpeciesService;
    private final SyncStarshipService syncStarshipService;
    private final SyncVehicleService syncVehicleService;

    public void syncAllData() {
        log.info("Starting full SWAPI sync...");
        try {
            syncPlanetService.syncPlanets();
            syncCharactersService.syncCharacters();
            syncFilmService.syncFilms();
            syncSpeciesService.syncSpecies();
            syncStarshipService.syncStarships();
            syncVehicleService.syncVehicles();
            log.info("SWAPI sync completed.");
        } catch (Exception ex) {
            log.error("SWAPI sync failed", ex);
            throw new SwapiSyncException("An error occurred during SWAPI synchronization", ex);
        }
    }
}
