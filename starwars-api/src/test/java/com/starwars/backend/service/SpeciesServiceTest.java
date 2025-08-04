package com.starwars.backend.service;

import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.exception.SpeciesRetrievalException;
import com.starwars.backend.persisntence.entities.*;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpeciesServiceTest {

    private SpeciesRepository speciesRepository;
    private SpeciesService speciesService;

    @BeforeEach
    void setUp() {
        speciesRepository = mock(SpeciesRepository.class);
        speciesService = new SpeciesService(speciesRepository);
    }

    @Test
    void testGetSpecies_noSearch_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 15);

        Planet homeworld = Planet.builder().name("Tatooine").build();
        Character luke = Character.builder().name("Luke").homeworld(homeworld).build();
        Film film = Film.builder().title("A New Hope").build();

        Species species = Species.builder()
                .name("Human")
                .classification("Mammal")
                .designation("Sentient")
                .averageHeight("180")
                .skinColors("fair")
                .hairColors("blond")
                .eyeColors("blue")
                .averageLifespan("120")
                .language("Galactic Basic")
                .characters(Set.of(luke))
                .films(Set.of(film))
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/species/1/")
                .build();

        Page<Species> page = new PageImpl<>(List.of(species));
        when(speciesRepository.findAll(pageable)).thenReturn(page);

        Page<SpeciesDTO> result = speciesService.getSpecies(null, pageable);

        assertEquals(1, result.getTotalElements());
        SpeciesDTO dto = result.getContent().get(0);
        assertEquals("Human", dto.name());
        assertEquals(List.of("Luke"), dto.people());
        assertEquals(List.of("A New Hope"), dto.films());
        assertEquals("Tatooine", dto.homeworld());
    }

    @Test
    void testGetSpecies_withSearch_shouldFilter() {
        Pageable pageable = PageRequest.of(0, 15);

        Species species = Species.builder()
                .name("Wookiee")
                .characters(Set.of())
                .films(Set.of())
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/species/2/")
                .build();

        Page<Species> page = new PageImpl<>(List.of(species));
        when(speciesRepository.findByNameContainingIgnoreCase("wookie", pageable)).thenReturn(page);

        Page<SpeciesDTO> result = speciesService.getSpecies("wookie", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Wookiee", result.getContent().get(0).name());
    }

    @Test
    void testGetSpecies_shouldReturnUnknownIfNoHomeworld() {
        Pageable pageable = PageRequest.of(0, 15);

        Character c = Character.builder().name("Chopper").homeworld(null).build();
        Species s = Species.builder()
                .name("Droid")
                .characters(Set.of(c))
                .films(Set.of())
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/species/3/")
                .build();

        when(speciesRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(s)));

        Page<SpeciesDTO> result = speciesService.getSpecies(null, pageable);

        assertEquals("Unknown", result.getContent().get(0).homeworld());
    }

    @Test
    void testGetSpecies_repositoryThrows_shouldThrowSpeciesRetrievalException() {
        Pageable pageable = PageRequest.of(0, 15);
        when(speciesRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        assertThrows(SpeciesRetrievalException.class, () -> {
            speciesService.getSpecies(null, pageable);
        });
    }
}
