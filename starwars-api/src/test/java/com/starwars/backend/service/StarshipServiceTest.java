package com.starwars.backend.service;

import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.exception.StarshipRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.repository.StarshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StarshipServiceTest {

    private StarshipRepository starshipRepository;
    private StarshipService starshipService;

    @BeforeEach
    void setUp() {
        starshipRepository = mock(StarshipRepository.class);
        starshipService = new StarshipService(starshipRepository);
    }

    @Test
    void testGetStarships_noSearch_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 15);

        Starship starship = Starship.builder()
                .name("X-Wing")
                .model("T-65")
                .manufacturer("Incom Corporation")
                .pilots(Set.of(Character.builder().name("Luke Skywalker").starships(Set.of()).build()))
                .films(Set.of(Film.builder().title("A New Hope").starships(Set.of()).build()))
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/starships/12/")
                .build();

        Page<Starship> page = new PageImpl<>(List.of(starship));
        when(starshipRepository.findAll(pageable)).thenReturn(page);

        Page<StarshipDTO> result = starshipService.getStarships(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("X-Wing", result.getContent().get(0).name());
        assertTrue(result.getContent().get(0).pilots().contains("Luke Skywalker"));
        assertTrue(result.getContent().get(0).films().contains("A New Hope"));
    }

    @Test
    void testGetStarships_withSearch_shouldUseRepositoryFilter() {
        Pageable pageable = PageRequest.of(0, 15);
        String search = "Falcon";

        when(starshipRepository.findByNameContainingIgnoreCase(eq(search), any()))
                .thenReturn(Page.empty());

        starshipService.getStarships(search, pageable);

        verify(starshipRepository).findByNameContainingIgnoreCase(eq(search), any());
    }

    @Test
    void testGetStarships_whenException_shouldThrowCustomException() {
        Pageable pageable = PageRequest.of(0, 15);
        when(starshipRepository.findAll((Pageable) any())).thenThrow(new RuntimeException("db down"));

        assertThrows(StarshipRetrievalException.class, () -> starshipService.getStarships(null, pageable));
    }
}
