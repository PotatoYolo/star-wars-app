package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.integration.SyncStarshipService;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.StarshipRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncStarshipServiceTest {

    private RestTemplate restTemplate;
    private StarshipRepository starshipRepository;
    private FilmRepository filmRepository;
    private CharacterRepository characterRepository;
    private SyncStarshipService syncStarshipService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        starshipRepository = mock(StarshipRepository.class);
        filmRepository = mock(FilmRepository.class);
        characterRepository = mock(CharacterRepository.class);
        syncStarshipService = new SyncStarshipService(restTemplate, starshipRepository,  characterRepository,filmRepository);
    }

    @Test
    void syncStarships_shouldSaveStarshipCorrectly() {
        String url = Utils.URL_STARSHIP;

        StarshipDTO dto = new StarshipDTO(
                "X-Wing", "T-65 X-wing", "Incom Corporation", "149999", "12.5", "1050",
                "1", "0", "110", "1 week", "1.0", "100", "Starfighter",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(), OffsetDateTime.now(), "https://swapi.dev/api/starships/1/"
        );

        SwapiResponse<StarshipDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        Character pilot = Character.builder().starships(new HashSet<>()).build();
        Film film = Film.builder().starships(new HashSet<>()).build();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(starshipRepository.findBySwapiId(1)).thenReturn(Optional.empty());
        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.of(pilot));
        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.of(film));

        when(starshipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncStarshipService.syncStarships();

        verify(starshipRepository, times(1)).save(any());
        assertEquals(1, pilot.getStarships().size());
        assertEquals(1, film.getStarships().size());
    }

    @Test
    void syncStarships_shouldSkipStarshipWithInvalidSwapiId() {
        String url = Utils.URL_STARSHIP;

        StarshipDTO dto = new StarshipDTO(
                "Unknown", "Unknown", "Unknown", "0", "0", "0", "0", "0", "0", "0", "0", "0", "Unknown",
                List.of(), List.of(), OffsetDateTime.now(), OffsetDateTime.now(), "invalid_url"
        );

        SwapiResponse<StarshipDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncStarshipService.syncStarships();

        verify(starshipRepository, never()).save(any());
    }

    @Test
    void syncStarships_shouldHandleEmptyResults() {
        String url = Utils.URL_STARSHIP;

        SwapiResponse<StarshipDTO> emptyResponse = new SwapiResponse<>(0, null, null, null);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        syncStarshipService.syncStarships();

        verify(starshipRepository, never()).save(any());
    }

    @Test
    void syncStarships_shouldStopOnException() {
        String url = Utils.URL_STARSHIP;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI error"));

        syncStarshipService.syncStarships();

        verify(starshipRepository, never()).save(any());
    }

    @Test
    void syncStarships_shouldStillSaveIfPilotsOrFilmsAreEmpty() {
        String url = Utils.URL_STARSHIP;

        StarshipDTO dto = new StarshipDTO(
                "TIE Fighter", "Twin Ion Engine", "Sienar Fleet Systems", "75000", "6.4", "1200",
                "1", "0", "65", "2 days", "1.5", "60", "Starfighter",
                Collections.emptyList(), Collections.emptyList(),
                OffsetDateTime.now(), OffsetDateTime.now(), "https://swapi.dev/api/starships/2/"
        );

        SwapiResponse<StarshipDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(starshipRepository.findBySwapiId(2)).thenReturn(Optional.empty());
        when(starshipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncStarshipService.syncStarships();

        verify(starshipRepository, times(1)).save(any());    }
}
