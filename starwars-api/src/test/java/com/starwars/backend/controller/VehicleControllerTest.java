package com.starwars.backend.controller;

import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.service.VehicleService;
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

@WebMvcTest(controllers = VehicleController.class)
@Import(VehicleControllerTest.Config.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleService vehicleService;

    @TestConfiguration
    static class Config {
        @Bean
        public VehicleService vehicleService() {
            return mock(VehicleService.class);
        }
    }

    @Test
    void listVehicles_shouldReturnOk() throws Exception {
        VehicleDTO dto = new VehicleDTO(
                "Sand Crawler", "Digger Crawler", "Corellia Mining Corporation", "150000",
                "wheeled", "36.8", "30", "46", "30", "50000", "2 months",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "https://swapi.dev/api/vehicles/4/"
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<VehicleDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(vehicleService.getVehicles("crawler", pageable)).thenReturn(page);

        mockMvc.perform(get("/api/vehicles")
                        .param("search", "crawler")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sand Crawler"));
    }

    @Test
    void listVehicles_shouldReturnInternalServerError_onException() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(vehicleService.getVehicles("crawler", pageable)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/vehicles")
                        .param("search", "crawler")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
