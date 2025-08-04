package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.CharacterDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.integration.SyncCharacterService;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

class SyncCharacterServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private CharacterRepository characterRepository;
    @Mock private PlanetRepository planetRepository;
    @Mock private FilmRepository filmRepository;
    @Mock private SpeciesRepository speciesRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private StarshipRepository starshipRepository;

    @InjectMocks private SyncCharacterService syncCharacterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSyncCharacters_successfulFlow() {
        String url = Utils.URL_PEOPLE;

        CharacterDTO dto = new CharacterDTO(
                "Luke Skywalker",
                "19BBY",
                "male",
                "172",
                "77",
                "blond",
                "fair",
                "blue",
                "https://swapi.dev/api/planets/1/",
                List.of("https://swapi.dev/api/films/1/"),
                List.of("https://swapi.dev/api/species/1/"),
                List.of("https://swapi.dev/api/vehicles/1/"),
                List.of("https://swapi.dev/api/starships/1/"),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/people/1/"
        );

        SwapiResponse<CharacterDTO> response = new SwapiResponse<>(
                1,
                null,
                null,
                List.of(dto)
        );

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(response));

        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.empty());
        when(planetRepository.findBySwapiId(1)).thenReturn(Optional.of(Planet.builder().name("Tatooine").build()));
        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.of(Film.builder().title("A New Hope").characters(new HashSet<>()).build()));
        when(speciesRepository.findBySwapiId(1)).thenReturn(Optional.of(Species.builder().name("Human").characters(new HashSet<>()).build()));
        when(vehicleRepository.findBySwapiId(1)).thenReturn(Optional.of(Vehicle.builder().name("Speeder").pilots(new HashSet<>()).build()));
        when(starshipRepository.findBySwapiId(1)).thenReturn(Optional.of(Starship.builder().name("X-Wing").pilots(new HashSet<>()).build()));

        when(characterRepository.save(any(Character.class))).thenAnswer(invocation -> invocation.getArgument(0));

        syncCharacterService.syncCharacters();

        verify(characterRepository).save(any(Character.class));
        verify(filmRepository).findBySwapiId(1);
        verify(speciesRepository).findBySwapiId(1);
        verify(vehicleRepository).findBySwapiId(1);
        verify(starshipRepository).findBySwapiId(1);
    }

    @Test
    void testSyncCharacters_withInvalidSwapiId_shouldSkipCharacter() {
        String url = Utils.URL_PEOPLE;

        CharacterDTO dto = new CharacterDTO(
                "Unknown Droid", "n/a", "n/a", "n/a", "n/a",
                null, null, null, null, null,
                null, null, null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "invalid_url"
        );

        SwapiResponse<CharacterDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncCharacterService.syncCharacters();

        verify(characterRepository, never()).save(any());
    }

    @Test
    void testSyncCharacters_withEmptyResults_shouldNotSave() {
        SwapiResponse<CharacterDTO> response = new SwapiResponse<>(0, null, null, null);
        String url = Utils.URL_PEOPLE;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncCharacterService.syncCharacters();

        verify(characterRepository, never()).save(any());
    }

    @Test
    void testSyncCharacters_withException_shouldLogAndStop() {
        String url = Utils.URL_PEOPLE;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI unavailable"));

        syncCharacterService.syncCharacters();

        verify(characterRepository, never()).save(any());
    }

}
