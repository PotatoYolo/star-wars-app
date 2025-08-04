package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.integration.SyncFilmService;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.PlanetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class SyncFilmServiceTest {

    private RestTemplate restTemplate;
    private FilmRepository filmRepository;
    private SyncFilmService syncFilmService;
    private CharacterRepository characterRepository;
    private PlanetRepository planetRepository;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        filmRepository = mock(FilmRepository.class);
        characterRepository = mock(CharacterRepository.class);
        planetRepository = mock(PlanetRepository.class);
        syncFilmService = new SyncFilmService(restTemplate, filmRepository, characterRepository, planetRepository);
    }

    @Test
    void syncFilms_shouldSaveFilm() {
        String url = Utils.URL_FILMS;

        FilmDTO filmDTO = new FilmDTO(
                "A New Hope",
                4,
                "Opening crawl",
                "George Lucas",
                "Gary Kurtz",
                "1977-05-25",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/films/1/"
        );


        SwapiResponse<FilmDTO> response = new SwapiResponse<>(1, null, null, List.of(filmDTO));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.empty());

        ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);

        syncFilmService.syncFilms();

        verify(filmRepository).save(filmCaptor.capture());
        Film saved = filmCaptor.getValue();

        assertEquals("A New Hope", saved.getTitle());
        assertEquals(4, saved.getEpisodeId());
        assertEquals("Opening crawl", saved.getOpeningCrawl());
        assertEquals("George Lucas", saved.getDirector());
        assertEquals("Gary Kurtz", saved.getProducer());
    }

    @Test
    void syncFilms_shouldSkipFilmWithInvalidSwapiId() {
        String url = Utils.URL_FILMS;

        FilmDTO filmDTO = new FilmDTO(
                "A New Hope",
                4,
                "Opening crawl",
                "George Lucas",
                "Gary Kurtz",
                "1977-05-25",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "invalid_url"
        );

        SwapiResponse<FilmDTO> response = new SwapiResponse<>(1, null, null, List.of(filmDTO));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncFilmService.syncFilms();

        verify(filmRepository, never()).save(any());
    }

    @Test
    void syncFilms_shouldHandleEmptyResults() {
        String url = Utils.URL_FILMS;

        SwapiResponse<FilmDTO> emptyResponse = new SwapiResponse<>(0, null, null, null);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        syncFilmService.syncFilms();

        verify(filmRepository, never()).save(any());
    }

    @Test
    void syncFilms_shouldStopOnException() {
        String url = Utils.URL_FILMS;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI is down"));

        syncFilmService.syncFilms();

        verify(filmRepository, never()).save(any());
    }

    @Test
    void syncFilms_shouldHandleInvalidReleaseDate() {
        String url = Utils.URL_FILMS;

        FilmDTO filmDTO = new FilmDTO(
                "Phantom Menace",
                1,
                "Long time ago...",
                "George Lucas",
                "Rick McCallum",
                "invalid-date",
                List.of(), List.of(), List.of(), List.of(), List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/films/4/"
        );

        SwapiResponse<FilmDTO> response = new SwapiResponse<>(1, null, null, List.of(filmDTO));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(filmRepository.findBySwapiId(4)).thenReturn(Optional.empty());

        syncFilmService.syncFilms();

        verify(filmRepository).save(any());
    }

    @Test
    void syncFilms_shouldMapCharactersAndPlanetsIfPresent() {
        String url = Utils.URL_FILMS;

        FilmDTO filmDTO = new FilmDTO(
                "Empire Strikes Back",
                5,
                "It is a dark time...",
                "Irvin Kershner",
                "Gary Kurtz",
                "1980-05-21",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/planets/2/"),
                List.of(), List.of(), List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/films/2/"
        );

        SwapiResponse<FilmDTO> response = new SwapiResponse<>(1, null, null, List.of(filmDTO));

        var character = com.starwars.backend.persisntence.entities.Character.builder().films(new HashSet<>()).build();
        var planet = com.starwars.backend.persisntence.entities.Planet.builder().films(new HashSet<>()).build();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(filmRepository.findBySwapiId(2)).thenReturn(Optional.empty());
        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.of(character));
        when(planetRepository.findBySwapiId(2)).thenReturn(Optional.of(planet));
        when(filmRepository.save(any())).thenAnswer(inv -> {
            Film film = inv.getArgument(0);
            film.setSwapiId(2);
            character.getFilms().add(film);
            planet.getFilms().add(film);
            return film;
        });

        syncFilmService.syncFilms();

        ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
        verify(filmRepository).save(filmCaptor.capture());
        Film savedFilm = filmCaptor.getValue();

        assertTrue(character.getFilms().stream().anyMatch(f -> f.getTitle().equals("Empire Strikes Back")));
        assertTrue(planet.getFilms().stream().anyMatch(f -> f.getTitle().equals("Empire Strikes Back")));
    }
}
