package com.starwars.backend.service;

import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.exception.PlanetRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.repository.PlanetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanetServiceTest {

    private PlanetRepository planetRepository;
    private PlanetService planetService;

    @BeforeEach
    void setUp() {
        planetRepository = mock(PlanetRepository.class);
        planetService = new PlanetService(planetRepository);
    }

    @Test
    void testGetPlanets_noSearch_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 15);
        Planet planet = Planet.builder()
                .name("Tatooine")
                .rotationPeriod("23")
                .orbitalPeriod("304")
                .diameter("10465")
                .climate("arid")
                .gravity("1 standard")
                .terrain("desert")
                .surfaceWater("1")
                .population("200000")
                .residents(Set.of(Character.builder().name("Luke Skywalker").build()))
                .films(Set.of(Film.builder().title("A New Hope").build()))
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/planets/1/")
                .build();

        Page<Planet> planetPage = new PageImpl<>(List.of(planet));
        when(planetRepository.findAll(pageable)).thenReturn(planetPage);

        Page<PlanetDTO> result = planetService.getPlanets(null, pageable);

        assertEquals(1, result.getTotalElements());
        PlanetDTO dto = result.getContent().get(0);
        assertEquals("Tatooine", dto.name());
        assertEquals(List.of("Luke Skywalker"), dto.residents());
        assertEquals(List.of("A New Hope"), dto.films());
    }

    @Test
    void testGetPlanets_withSearch_shouldFilterByName() {
        Pageable pageable = PageRequest.of(0, 15);
        Planet planet = Planet.builder()
                .name("Hoth")
                .residents(Set.of())
                .films(Set.of())
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/planets/4/")
                .build();

        Page<Planet> planetPage = new PageImpl<>(List.of(planet));
        when(planetRepository.findByNameContainingIgnoreCase("hoth", pageable)).thenReturn(planetPage);

        Page<PlanetDTO> result = planetService.getPlanets("hoth", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Hoth", result.getContent().get(0).name());
    }

    @Test
    void testGetPlanets_repositoryThrowsException_shouldThrowPlanetRetrievalException() {
        Pageable pageable = PageRequest.of(0, 15);
        when(planetRepository.findAll(pageable)).thenThrow(new RuntimeException("DB Error"));

        PlanetRetrievalException exception = assertThrows(
                PlanetRetrievalException.class,
                () -> planetService.getPlanets(null, pageable)
        );

        assertTrue(exception.getMessage().contains("Unable to retrieve planets"));
    }
}
