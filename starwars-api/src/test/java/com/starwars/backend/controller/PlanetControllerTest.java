package com.starwars.backend.controller;

import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.service.PlanetService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlanetController.class)
@Import(PlanetControllerTest.Config.class)
class PlanetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanetService planetService;

    @TestConfiguration
    static class Config {
        @Bean
        public PlanetService planetService() {
            return mock(PlanetService.class);
        }
    }

    @Test
    void getPlanets_shouldReturnOk() throws Exception {
        PlanetDTO dto = new PlanetDTO(
                "Tatooine", "23", "304", "10465", "arid", "1 standard", "desert",
                "1", "200000", List.of(), List.of(),
                OffsetDateTime.now(), OffsetDateTime.now(), "https://swapi.dev/api/planets/1/"
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<PlanetDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(planetService.getPlanets("tatooine", pageable)).thenReturn(page);

        mockMvc.perform(get("/api/planets")
                        .param("search", "tatooine")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Tatooine"));
    }

    @Test
    void getPlanets_shouldReturnInternalServerError_onException() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(planetService.getPlanets("tatooine", pageable)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/planets")
                        .param("search", "tatooine")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
