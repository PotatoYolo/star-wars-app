package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.integration.SyncSpeciesService;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.SpeciesRepository;
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

class SyncSpeciesServiceTest {

    private RestTemplate restTemplate;
    private SpeciesRepository speciesRepository;
    private CharacterRepository characterRepository;
    private FilmRepository filmRepository;
    private SyncSpeciesService syncSpeciesService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        speciesRepository = mock(SpeciesRepository.class);
        characterRepository = mock(CharacterRepository.class);
        filmRepository = mock(FilmRepository.class);
        syncSpeciesService = new SyncSpeciesService(restTemplate, speciesRepository, characterRepository, filmRepository);
    }

    @Test
    void syncSpecies_shouldSaveSpeciesCorrectly() {
        String url = Utils.URL_SPECIES;

        SpeciesDTO dto = new SpeciesDTO(
                "Human", "mammal", "sentient", "180", "light", "brown", "blue", "80", "Galactic Basic",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/species/1/",
                null
        );

        SwapiResponse<SpeciesDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        Species savedSpecies = Species.builder()
                .characters(new HashSet<>())
                .films(new HashSet<>())
                .build();

        Character character = Character.builder()
                .species(new HashSet<>())
                .build();

        Film film = Film.builder()
                .species(new HashSet<>())
                .build();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(speciesRepository.findBySwapiId(1)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenReturn(savedSpecies);
        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.of(character));
        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.of(film));

        syncSpeciesService.syncSpecies();

        verify(speciesRepository, atLeastOnce()).save(any());
        assertEquals(1, character.getSpecies().size());
        assertEquals(1, film.getSpecies().size());
    }

    @Test
    void syncSpecies_shouldSkipSpeciesWithInvalidSwapiId() {
        String url = Utils.URL_SPECIES;

        SpeciesDTO dto = new SpeciesDTO(
                "Unknown", "unknown", "unknown", "0", "none", "none", "none", "0", "none",
                List.of(), List.of(),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "invalid_url", null
        );

        SwapiResponse<SpeciesDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncSpeciesService.syncSpecies();

        verify(speciesRepository, never()).save(any());
    }

    @Test
    void syncSpecies_shouldHandleEmptyResults() {
        String url = Utils.URL_SPECIES;

        SwapiResponse<SpeciesDTO> emptyResponse = new SwapiResponse<>(0, null, null, null);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        syncSpeciesService.syncSpecies();

        verify(speciesRepository, never()).save(any());
    }

    @Test
    void syncSpecies_shouldStopOnException() {
        String url = Utils.URL_SPECIES;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI error"));

        syncSpeciesService.syncSpecies();

        verify(speciesRepository, never()).save(any());
    }

    @Test
    void syncSpecies_shouldStillSaveIfPeopleOrFilmsAreEmpty() {
        String url = Utils.URL_SPECIES;

        SpeciesDTO dto = new SpeciesDTO(
                "Droid", "artificial", "sentient", "n/a", "none", "none", "none", "indefinite", "Binary",
                Collections.emptyList(),
                Collections.emptyList(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/species/2/",
                null
        );

        SwapiResponse<SpeciesDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(speciesRepository.findBySwapiId(2)).thenReturn(Optional.empty());

        syncSpeciesService.syncSpecies();

        verify(speciesRepository, atLeastOnce()).save(any());    }
}
