package com.starwars.backend.controller;

import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.service.FilmService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FilmController.class)
@Import(FilmControllerTest.Config.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmService filmService;

    @TestConfiguration
    static class Config {
        @Bean
        public FilmService filmService() {
            return mock(FilmService.class);
        }
    }

    @Test
    void listFilms_shouldReturnOk() throws Exception {
        FilmDTO dto = new FilmDTO(
                "A New Hope",
                4,
                "It is a period of civil war...",
                "George Lucas",
                "Gary Kurtz, Rick McCallum",
                "1977-05-25",
                List.of("https://swapi.dev/api/characters/1/"),
                List.of("https://swapi.dev/api/planets/1/"),
                List.of("https://swapi.dev/api/starships/1/"),
                List.of("https://swapi.dev/api/vehicles/1/"),
                List.of("https://swapi.dev/api/species/1/"),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "https://swapi.dev/api/films/1/"
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<FilmDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(filmService.getFilms("hope", pageable)).thenReturn(page);

        mockMvc.perform(get("/api/films")
                        .param("search", "hope")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("A New Hope"));
    }

    @Test
    void listFilms_shouldReturnInternalServerError_onException() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(filmService.getFilms("hope", pageable)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/films")
                        .param("search", "hope")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
