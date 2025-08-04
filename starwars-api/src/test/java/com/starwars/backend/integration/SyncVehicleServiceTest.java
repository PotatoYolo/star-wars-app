package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.integration.SyncVehicleService;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.VehicleRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncVehicleServiceTest {

    private RestTemplate restTemplate;
    private VehicleRepository vehicleRepository;
    private CharacterRepository characterRepository;
    private FilmRepository filmRepository;
    private SyncVehicleService syncVehicleService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        vehicleRepository = mock(VehicleRepository.class);
        characterRepository = mock(CharacterRepository.class);
        filmRepository = mock(FilmRepository.class);
        syncVehicleService = new SyncVehicleService(restTemplate, vehicleRepository, characterRepository, filmRepository);
    }

    @Test
    void syncVehicles_shouldSaveVehicleCorrectly() {
        String url = Utils.URL_VEHICLES;

        VehicleDTO dto = new VehicleDTO(
                "Speeder", "X-34 landspeeder", "SoroSuub Corporation", "10550", "repulsorcraft",
                "3.4", "250", "1", "1", "5", "none",
                List.of("https://swapi.dev/api/people/1/"),
                List.of("https://swapi.dev/api/films/1/"),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "https://swapi.dev/api/vehicles/1/"
        );

        SwapiResponse<VehicleDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        Character pilot = Character.builder().vehicles(new HashSet<>()).build();
        Film film = Film.builder().vehicles(new HashSet<>()).build();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(vehicleRepository.findBySwapiId(1)).thenReturn(Optional.empty());
        when(characterRepository.findBySwapiId(1)).thenReturn(Optional.of(pilot));
        when(filmRepository.findBySwapiId(1)).thenReturn(Optional.of(film));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncVehicleService.syncVehicles();

        verify(vehicleRepository, times(1)).save(any());        assertEquals(1, pilot.getVehicles().size());
        assertEquals(1, film.getVehicles().size());
    }

    @Test
    void syncVehicles_shouldSkipVehicleWithInvalidSwapiId() {
        String url = Utils.URL_VEHICLES;

        VehicleDTO dto = new VehicleDTO(
                "Unknown", "Unknown", "Unknown", "0", "unknown", "0", "0", "0", "0", "0", "0",
                List.of(), List.of(),
                OffsetDateTime.now(), OffsetDateTime.now(), "invalid_url"
        );

        SwapiResponse<VehicleDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        syncVehicleService.syncVehicles();

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void syncVehicles_shouldHandleEmptyResults() {
        String url = Utils.URL_VEHICLES;

        SwapiResponse<VehicleDTO> emptyResponse = new SwapiResponse<>(0, null, null, null);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        syncVehicleService.syncVehicles();

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void syncVehicles_shouldStopOnException() {
        String url = Utils.URL_VEHICLES;

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("SWAPI error"));

        syncVehicleService.syncVehicles();

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void syncVehicles_shouldStillSaveIfPilotsOrFilmsAreEmpty() {
        String url = Utils.URL_VEHICLES;

        VehicleDTO dto = new VehicleDTO(
                "AT-AT", "All Terrain Armored Transport", "Kuat Drive Yards", "200000", "assault walker",
                "20", "60", "5", "40", "1000", "2 months",
                Collections.emptyList(),
                Collections.emptyList(),
                OffsetDateTime.now(), OffsetDateTime.now(),
                "https://swapi.dev/api/vehicles/2/"
        );

        SwapiResponse<VehicleDTO> response = new SwapiResponse<>(1, null, null, List.of(dto));

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        when(vehicleRepository.findBySwapiId(2)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncVehicleService.syncVehicles();

        verify(vehicleRepository, times(1)).save(any());    }
}
