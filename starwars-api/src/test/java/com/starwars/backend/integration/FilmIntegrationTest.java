package com.starwars.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.repository.FilmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilmIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        filmRepository.deleteAll();

        Film film1 = Film.builder()
                .title("A New Hope")
                .director("George Lucas")
                .producer("Gary Kurtz")
                .releaseDate(OffsetDateTime.parse("1977-05-25T00:00:00Z"))
                .openingCrawl("It is a period of civil war...")
                .swapiId(1)
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/films/1/")
                .characters(new HashSet<>())
                .vehicles(new HashSet<>())
                .starships(new HashSet<>())
                .species(new HashSet<>())
                .planets(new HashSet<>())
                .build();

        Film film2 = Film.builder()
                .title("The Empire Strikes Back")
                .director("Irvin Kershner")
                .producer("Gary Kurtz")
                .releaseDate(OffsetDateTime.parse("1980-05-17T00:00:00Z"))
                .openingCrawl("It is a dark time for the Rebellion...")
                .swapiId(2)
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/films/2/")
                .characters(new HashSet<>())
                .vehicles(new HashSet<>())
                .starships(new HashSet<>())
                .species(new HashSet<>())
                .planets(new HashSet<>())
                .build();

        filmRepository.saveAll(List.of(film1, film2));
    }

    @Test
    @DisplayName("shouldReturnAllFilms")
    void shouldReturnAllFilms() throws Exception {
        mockMvc.perform(get("/api/films")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("shouldReturnFilteredFilmsByTitle")
    void shouldReturnFilteredFilmsByTitle() throws Exception {
        mockMvc.perform(get("/api/films")
                        .param("search", "Empire")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Empire Strikes Back")));
    }

    @Test
    @DisplayName("shouldReturnEmptyResultWhenNoMatch")
    void shouldReturnEmptyResultWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/films")
                        .param("search", "Phantom")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("shouldHandleMissingOptionalFieldsGracefully")
    void shouldHandleMissingOptionalFieldsGracefully() throws Exception {
        filmRepository.deleteAll();

        Film incomplete = Film.builder()
                .title("Test Film")
                .swapiId(99)
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/films/99/")
                .characters(new HashSet<>())
                .vehicles(new HashSet<>())
                .starships(new HashSet<>())
                .species(new HashSet<>())
                .planets(new HashSet<>())
                .build();

        filmRepository.save(incomplete);

        mockMvc.perform(get("/api/films")
                        .param("search", "Test")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Test Film")));
    }
}
