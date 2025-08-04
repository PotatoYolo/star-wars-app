package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.integration.SyncPlanetService;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.PlanetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncPlanetServiceTest {

    private RestTemplate restTemplate;
    private PlanetRepository planetRepository;
    private CharacterRepository characterRepository;
    private FilmRepository filmRepository;
    private SyncPlanetService syncPlanetService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        planetRepository = mock(PlanetRepository.class);
        characterRepository = mock(CharacterRepository.class);
        filmRepository = mock(FilmRepository.class);
        syncPlanetService = new SyncPlanetService(restTemplate, planetRepository, characterRepository, filmRepository);
    }

    @Test
    void syncPlanets_shouldSavePlanetCorrectly() {
        String url = Utils.URL_PLANETS;

        PlanetDTO dto = new PlanetDTO(
                "Tatooine", "23", "304", "10465", "arid", "1 standard", "desert", "1", "200000",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/planets/1/"
        );

        SwapiResponse<PlanetDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        Planet planet = Planet.builder().residents(new HashSet<>()).films(new HashSet<>()).build();
        Character character = Character.builder().build();
        Film film = Film.builder().planets(new HashSet<>()).build();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(planetRepository.findBySwapiId(1)).thenReturn(Optional.empty());
        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.of(character));
        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.of(film));

        syncPlanetService.syncPlanets();

        verify(planetRepository).save(any());
        Planet savedPlanet = character.getHomeworld();
        assertNotNull(savedPlanet);
        assertEquals("Tatooine", savedPlanet.getName());
        assertTrue(savedPlanet.getResidents().contains(character));
        assertTrue(film.getPlanets().stream().anyMatch(p -> p.getSwapiId() == 1));
    }

    @Test
    void syncPlanets_shouldSkipPlanetWithInvalidSwapiId() {
        String url = Utils.URL_PLANETS;

        PlanetDTO dto = new PlanetDTO(
                "Unknown", "0", "0", "0", "none", "none", "none", "0", "0",
                null, null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "invalid_url"
        );

        SwapiResponse<PlanetDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncPlanetService.syncPlanets();

        verify(planetRepository, never()).save(any());
    }

    @Test
    void syncPlanets_shouldHandleEmptyResults() {
        String url = Utils.URL_PLANETS;

        SwapiResponse<PlanetDTO> emptyResponse = new SwapiResponse<>(0, null, null, null);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        syncPlanetService.syncPlanets();

        verify(planetRepository, never()).save(any());
    }

    @Test
    void syncPlanets_shouldStopOnException() {
        String url = Utils.URL_PLANETS;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI error"));

        syncPlanetService.syncPlanets();

        verify(planetRepository, never()).save(any());
    }

    @Test
    void syncPlanets_shouldStillSaveIfResidentsOrFilmsAreNull() {
        String url = Utils.URL_PLANETS;

        PlanetDTO dto = new PlanetDTO(
                "Dagobah", "23", "304", "10465", "murky", "1 standard", "swamp", "1", "0",
                null, null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/planets/5/"
        );

        SwapiResponse<PlanetDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(planetRepository.findBySwapiId(5)).thenReturn(Optional.empty());

        syncPlanetService.syncPlanets();

        verify(planetRepository).save(any());
    }
}
