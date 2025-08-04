package com.starwars.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.repository.StarshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StarshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StarshipRepository starshipRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        starshipRepository.deleteAll();

        Starship s1 = Starship.builder()
                .swapiId(1)
                .name("X-Wing")
                .model("T-65B")
                .manufacturer("Incom Corporation")
                .costInCredits("149999")
                .length("12.5")
                .maxAtmospheringSpeed("1050")
                .crew("1")
                .passengers("0")
                .cargoCapacity("110")
                .consumables("1 week")
                .hyperdriveRating("1.0")
                .mglt("100")
                .starshipClass("Starfighter")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/starships/1/")
                .build();

        Starship s2 = Starship.builder()
                .swapiId(2)
                .name("Millennium Falcon")
                .model("YT-1300 light freighter")
                .manufacturer("Corellian Engineering Corporation")
                .costInCredits("100000")
                .length("34.75")
                .maxAtmospheringSpeed("1050")
                .crew("4")
                .passengers("6")
                .cargoCapacity("100000")
                .consumables("2 months")
                .hyperdriveRating("0.5")
                .mglt("75")
                .starshipClass("Light freighter")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/starships/2/")
                .build();

        starshipRepository.saveAll(Set.of(s1, s2));
    }

    @Test
    void shouldReturnAllStarships() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldReturnFilteredStarshipsByName() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("search", "X-Wing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("X-Wing"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturnEmptyResultWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("search", "Imperial Shuttle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldHandleMissingOptionalFieldsGracefully() throws Exception {
        Starship s3 = Starship.builder()
                .swapiId(3)
                .name("Ghost")
                .model("VCX-100")
                .manufacturer("Corellian Engineering")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/starships/3/")
                .build();

        starshipRepository.save(s3);

        mockMvc.perform(get("/api/starships")
                        .param("search", "Ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ghost"))
                .andExpect(jsonPath("$.content[0].model").value("VCX-100"))
                .andExpect(jsonPath("$.content[0].manufacturer").value("Corellian Engineering"));
    }

    @Test
    void shouldReturnPilotsAndFilmsAsArrays() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("search", "Millennium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pilots").isArray())
                .andExpect(jsonPath("$.content[0].films").isArray());
    }
}
