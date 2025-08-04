package com.starwars.backend.service;

import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.exception.VehicleRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    private VehicleRepository vehicleRepository;
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleRepository = mock(VehicleRepository.class);
        vehicleService = new VehicleService(vehicleRepository);
    }

    @Test
    void testGetVehicles_noSearch_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 15);

        Vehicle vehicle = Vehicle.builder()
                .name("Speeder Bike")
                .model("74-Z")
                .manufacturer("Aratech")
                .vehicleClass("Speeder")
                .pilots(Set.of(Character.builder().name("Scout Trooper").vehicles(Set.of()).build()))
                .films(Set.of(Film.builder().title("Return of the Jedi").vehicles(Set.of()).build()))
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/vehicles/44/")
                .build();

        Page<Vehicle> page = new PageImpl<>(List.of(vehicle));
        when(vehicleRepository.findAll(pageable)).thenReturn(page);

        Page<VehicleDTO> result = vehicleService.getVehicles(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Speeder Bike", result.getContent().get(0).name());
        assertTrue(result.getContent().get(0).pilots().contains("Scout Trooper"));
        assertTrue(result.getContent().get(0).films().contains("Return of the Jedi"));
    }

    @Test
    void testGetVehicles_withSearch_shouldUseRepositoryFilter() {
        Pageable pageable = PageRequest.of(0, 15);
        String search = "Speeder";

        when(vehicleRepository.findByNameContainingIgnoreCase(eq(search), any()))
                .thenReturn(Page.empty());

        vehicleService.getVehicles(search, pageable);

        verify(vehicleRepository).findByNameContainingIgnoreCase(eq(search), any());
    }

    @Test
    void testGetVehicles_whenException_shouldThrowCustomException() {
        Pageable pageable = PageRequest.of(0, 15);
        when(vehicleRepository.findAll((Pageable) any())).thenThrow(new RuntimeException("db error"));

        assertThrows(VehicleRetrievalException.class, () -> vehicleService.getVehicles(null, pageable));
    }
}
