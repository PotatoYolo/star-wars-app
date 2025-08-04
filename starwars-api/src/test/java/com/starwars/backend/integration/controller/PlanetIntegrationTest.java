package com.starwars.backend.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.dto.PageResponse;
import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.repository.PlanetRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanetRepository planetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        planetRepository.deleteAll();
        planetRepository.save(Planet.builder()
                .swapiId(1)
                .name("Tatooine")
                .rotationPeriod("23")
                .orbitalPeriod("304")
                .diameter("10465")
                .climate("arid")
                .gravity("1 standard")
                .terrain("desert")
                .surfaceWater("1")
                .population("200000")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/planets/1/")
                .build());
    }

    @Test
    void shouldReturnPagedPlanets() throws Exception {
        var result = mockMvc.perform(get("/api/planets")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Tatooine"))
                .andReturn();

        var response = result.getResponse().getContentAsString();

        PageResponse<PlanetDTO> page = objectMapper.readValue(
                response,
                new TypeReference<PageResponse<PlanetDTO>>() {}
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyWhenNoPlanets() throws Exception {
        planetRepository.deleteAll();
        mockMvc.perform(get("/api/planets")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnFilteredPlanetsBySearchTerm() throws Exception {
        planetRepository.save(Planet.builder()
                .swapiId(2)
                .name("Alderaan")
                .climate("temperate")
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/planets/2/")
                .build());

        var result = mockMvc.perform(get("/api/planets")
                        .param("search", "Alderaan")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alderaan"))
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<PlanetDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldPaginateCorrectly() throws Exception {
        planetRepository.deleteAll();
        for (int i = 1; i <= 20; i++) {
            planetRepository.save(Planet.builder()
                    .swapiId(100 + i)
                    .name("Planet" + i)
                    .climate("unknown")
                    .created(OffsetDateTime.now())
                    .edited(OffsetDateTime.now())
                    .url("https://swapi.dev/api/planets/" + i + "/")
                    .build());
        }

        var result = mockMvc.perform(get("/api/planets")
                        .param("page", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<PlanetDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getContent().size()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    void shouldSortPlanetsByNameAsc() throws Exception {
        planetRepository.save(Planet.builder().swapiId(10).name("Yavin").created(OffsetDateTime.now()).edited(OffsetDateTime.now()).url("https://...").build());
        planetRepository.save(Planet.builder().swapiId(11).name("Alderaan").created(OffsetDateTime.now()).edited(OffsetDateTime.now()).url("https://...").build());

        var result = mockMvc.perform(get("/api/planets")
                        .param("sort", "name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<PlanetDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getContent().getFirst().name()).isEqualTo("Alderaan");
    }

    @Test
    void shouldHandleEmptyFilmsAndResidentsGracefully() throws Exception {
        planetRepository.save(Planet.builder()
                .swapiId(20)
                .name("Endor")
                .films(Set.of())
                .residents(Set.of())
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/planets/20/")
                .build());

        mockMvc.perform(get("/api/planets")
                        .param("search", "Endor")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].films").isArray())
                .andExpect(jsonPath("$.content[0].residents").isArray());
    }

    @Test
    void shouldReturnEmptyListIfNoMatchesFound() throws Exception {
        mockMvc.perform(get("/api/planets")
                        .param("search", "NonExistentPlanet")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
