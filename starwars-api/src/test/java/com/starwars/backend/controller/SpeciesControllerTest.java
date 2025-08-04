package com.starwars.backend.controller;

import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.service.SpeciesService;
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

@WebMvcTest(controllers = SpeciesController.class)
@Import(SpeciesControllerTest.Config.class)
class SpeciesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpeciesService speciesService;

    @TestConfiguration
    static class Config {
        @Bean
        public SpeciesService speciesService() {
            return mock(SpeciesService.class);
        }
    }

    @Test
    void listSpecies_shouldReturnOk() throws Exception {
        SpeciesDTO dto = new SpeciesDTO(
                "Wookiee", "mammal", "sentient", "210", "gray", "black", "blue",
                "400", "Shyriiwook",
                List.of("https://swapi.dev/api/people/13/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "https://swapi.dev/api/species/3/",
                "https://swapi.dev/api/planets/14/"
        );

        Pageable pageable = PageRequest.of(0, 15);
        Page<SpeciesDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(speciesService.getSpecies(eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/species")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wookiee"));
    }

    @Test
    void listSpecies_shouldReturnInternalServerError_onException() throws Exception {
        Pageable pageable = PageRequest.of(0, 15);
        when(speciesService.getSpecies(null, pageable)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/species")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}