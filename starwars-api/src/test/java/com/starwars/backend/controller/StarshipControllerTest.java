package com.starwars.backend.controller;

import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.service.StarshipService;
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

@WebMvcTest(controllers = StarshipController.class)
@Import(StarshipControllerTest.Config.class)
class StarshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StarshipService starshipService;

    @TestConfiguration
    static class Config {
        @Bean
        public StarshipService starshipService() {
            return mock(StarshipService.class);
        }
    }

    @Test
    void listStarships_shouldReturnOk() throws Exception {
        StarshipDTO dto = new StarshipDTO(
                "X-Wing", "T-65 X-wing", "Incom Corporation", "149999", "12.5", "1050",
                "1", "0", "110", "1 week", "1.0", "100", "Starfighter",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "https://swapi.dev/api/starships/12/"
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<StarshipDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(starshipService.getStarships("x-wing", pageable)).thenReturn(page);

        mockMvc.perform(get("/api/starships")
                        .param("search", "x-wing")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("X-Wing"));
    }

    @Test
    void listStarships_shouldReturnInternalServerError_onException() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(starshipService.getStarships("x-wing", pageable)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/starships")
                        .param("search", "x-wing")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
