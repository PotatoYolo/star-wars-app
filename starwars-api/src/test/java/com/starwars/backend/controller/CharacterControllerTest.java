package com.starwars.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.dto.form.CharacterFormDTO;
import com.starwars.backend.service.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterController.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public CharacterService characterService() {
            return Mockito.mock(CharacterService.class);
        }
    }

    private CharacterFormDTO mockDto;

    @BeforeEach
    void setUp() {
        mockDto = new CharacterFormDTO();
        mockDto.setName("Luke Skywalker");
        mockDto.setHomeworldId(1L);
        mockDto.setFilmIds(Set.of(1L));
        mockDto.setSpeciesIds(Set.of(1L));
        mockDto.setVehicleIds(Set.of(1L));
        mockDto.setStarshipIds(Set.of(1L));
    }

    @Test
    void getCharacters_shouldReturnOk() throws Exception {
        Page<CharacterFormDTO> page = new PageImpl<>(List.of(mockDto));
        when(characterService.getCharacters(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/characters")
                        .param("search", "Luke")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"));
    }

    @Test
    void createCharacter_shouldReturnCreated() throws Exception {
        when(characterService.createCharacter(any())).thenReturn(mockDto);

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Luke Skywalker"));
    }

    @Test
    void updateCharacter_shouldReturnOk() throws Exception {
        when(characterService.updateCharacter(eq(1L), any())).thenReturn(new com.starwars.backend.persisntence.entities.Character());
        when(characterService.toDto(any())).thenReturn(mockDto);

        mockMvc.perform(put("/api/characters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luke Skywalker"));
    }

    @Test
    void deleteCharacter_shouldReturnNoContent() throws Exception {
        doNothing().when(characterService).deleteCharacter(1L);

        mockMvc.perform(delete("/api/characters/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSupportData_shouldReturnMap() throws Exception {
        Map<String, Object> fakeData = Map.of("films", List.of(Map.of("id", 1, "title", "A New Hope")));
        when(characterService.getSupportData()).thenReturn(fakeData);

        mockMvc.perform(get("/api/characters/support-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.films[0].title").value("A New Hope"));
    }
}
