package com.starwars.backend.service;

import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.exception.FilmRetrievalException;
import com.starwars.backend.persisntence.entities.*;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.FilmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class FilmServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @InjectMocks
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void testGetFilms_withSearch_shouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 15, Sort.unsorted());

        Character luke = Character.builder().name("Luke Skywalker").build();
        Planet tatooine = Planet.builder().name("Tatooine").build();
        Starship xwing = Starship.builder().name("X-Wing").build();
        Vehicle speeder = Vehicle.builder().name("Speeder").build();
        Species human = Species.builder().name("Human").build();

        Film film = Film.builder()
                .title("A New Hope")
                .episodeId(4)
                .director("George Lucas")
                .producer("Lucasfilm")
                .releaseDate(OffsetDateTime.now())
                .characters(Set.of(luke))
                .planets(Set.of(tatooine))
                .starships(Set.of(xwing))
                .vehicles(Set.of(speeder))
                .species(Set.of(human))
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/films/1/")
                .build();

        Page<Film> filmPage = new PageImpl<>(List.of(film));
        when(filmRepository.findByTitleContainingIgnoreCase("hope", pageable)).thenReturn(filmPage);

        Page<FilmDTO> result = filmService.getFilms("hope", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("A New Hope", result.getContent().get(0).title());
    }

    @Test
    void testGetFilms_noSearch_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 15, Sort.unsorted());

        Film film = Film.builder()
                .title("Empire Strikes Back")
                .characters(new HashSet<>())
                .planets(new HashSet<>())
                .starships(new HashSet<>())
                .vehicles(new HashSet<>())
                .species(new HashSet<>())
                .build();

        Page<Film> filmPage = new PageImpl<>(List.of(film));
        when(filmRepository.findAll(pageable)).thenReturn(filmPage);

        Page<FilmDTO> result = filmService.getFilms(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Empire Strikes Back", result.getContent().get(0).title());
    }

    @Test
    void testGetFilms_exceptionThrown_shouldThrowCustomException() {
        Pageable pageable = PageRequest.of(0, 15, Sort.unsorted());
        when(filmRepository.findAll(pageable)).thenThrow(new RuntimeException("DB error"));

        assertThrows(FilmRetrievalException.class, () -> filmService.getFilms(null, pageable));
    }
}
