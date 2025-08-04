package com.starwars.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.repository.SpeciesRepository;
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
class SpeciesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        speciesRepository.deleteAll();

        Species species1 = Species.builder()
                .name("Wookiee")
                .classification("Mammal")
                .designation("Sentient")
                .averageHeight("210")
                .skinColors("gray")
                .hairColors("brown, black")
                .eyeColors("blue, green, yellow, brown, golden, red")
                .averageLifespan("400")
                .language("Shyriiwook")
                .homeworld("Kashyyyk")
                .swapiId(1)
                .url("https://swapi.dev/api/species/3/")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .characters(new HashSet<>())
                .films(new HashSet<>())
                .build();

        Species species2 = Species.builder()
                .name("Droid")
                .classification("Artificial")
                .designation("Sentient")
                .averageHeight("n/a")
                .skinColors("n/a")
                .hairColors("n/a")
                .eyeColors("n/a")
                .averageLifespan("indefinite")
                .language("n/a")
                .homeworld("unknown")
                .swapiId(2)
                .url("https://swapi.dev/api/species/2/")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .characters(new HashSet<>())
                .films(new HashSet<>())
                .build();

        speciesRepository.saveAll(List.of(species1, species2));
    }

    @Test
    @DisplayName("shouldReturnAllSpecies")
    void shouldReturnAllSpecies() throws Exception {
        mockMvc.perform(get("/api/species")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("shouldReturnFilteredSpeciesByName")
    void shouldReturnFilteredSpeciesByName() throws Exception {
        mockMvc.perform(get("/api/species")
                        .param("search", "Wookiee")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Wookiee")));
    }

    @Test
    @DisplayName("shouldReturnEmptyResultWhenNoMatch")
    void shouldReturnEmptyResultWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/species")
                        .param("search", "Gungan")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("shouldHandleMissingOptionalFieldsGracefully")
    void shouldHandleMissingOptionalFieldsGracefully() throws Exception {
        speciesRepository.deleteAll();

        Species incomplete = Species.builder()
                .name("Unknown Species")
                .swapiId(999)
                .url("https://swapi.dev/api/species/999/")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .characters(new HashSet<>())
                .films(new HashSet<>())
                .build();

        speciesRepository.save(incomplete);

        mockMvc.perform(get("/api/species")
                        .param("search", "Unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("Unknown Species")));
    }
}
