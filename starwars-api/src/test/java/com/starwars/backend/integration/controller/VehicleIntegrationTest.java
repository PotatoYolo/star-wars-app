package com.starwars.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        vehicleRepository.saveAll(List.of(
                Vehicle.builder()
                        .name("AT-AT")
                        .swapiId(999)
                        .model("All Terrain Armored Transport")
                        .manufacturer("Kuat Drive Yards")
                        .vehicleClass("assault walker")
                        .url("https://swapi.dev/api/vehicles/1/")
                        .build(),
                Vehicle.builder()
                        .name("Speeder Bike")
                        .swapiId(888)
                        .model("74-Z")
                        .manufacturer("Aratech Repulsor Company")
                        .vehicleClass("speeder")
                        .url("https://swapi.dev/api/vehicles/2/")
                        .build()
        ));
    }

    @Test
    void shouldReturnAllVehicles() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterVehiclesByName() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .param("search", "AT-AT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("AT-AT")));
    }

    @Test
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .param("search", "banana")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
