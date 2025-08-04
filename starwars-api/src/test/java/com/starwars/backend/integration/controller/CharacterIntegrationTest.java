package com.starwars.backend.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.dto.CharacterDTO;
import com.starwars.backend.dto.PageResponse;
import com.starwars.backend.dto.form.CharacterFormDTO;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        characterRepository.deleteAll();
        characterRepository.saveAll(List.of(
                Character.builder()
                        .name("Luke Skywalker")
                        .swapiId(1)
                        .created(OffsetDateTime.now())
                        .edited(OffsetDateTime.now())
                        .url("https://swapi.dev/api/people/1/")
                        .build(),
                Character.builder()
                        .name("Leia Organa")
                        .swapiId(2)
                        .created(OffsetDateTime.now())
                        .edited(OffsetDateTime.now())
                        .url("https://swapi.dev/api/people/2/")
                        .build(),
                Character.builder()
                        .name("Anakin Skywalker")
                        .swapiId(3)
                        .created(OffsetDateTime.now())
                        .edited(OffsetDateTime.now())
                        .url("https://swapi.dev/api/people/3/")
                        .build()
        ));
    }

    @Test
    void shouldReturnPagedCharacters() throws Exception {
        var result = mockMvc.perform(get("/api/characters")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<CharacterFormDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyWhenNoCharacters() throws Exception {
        characterRepository.deleteAll();
        mockMvc.perform(get("/api/characters")
                        .param("page", "0")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnCharactersMatchingSearch() throws Exception {
        var result = mockMvc.perform(get("/api/characters")
                        .param("search", "sky")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<CharacterFormDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListIfNoMatchesFound() throws Exception {
        mockMvc.perform(get("/api/characters")
                        .param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldPaginateCorrectly() throws Exception {
        characterRepository.deleteAll();
        for (int i = 1; i <= 20; i++) {
            characterRepository.save(Character.builder()
                    .name("Character" + i)
                    .swapiId(1000 + i)
                    .created(OffsetDateTime.now())
                    .edited(OffsetDateTime.now())
                    .url("https://swapi.dev/api/people/" + i + "/")
                    .build());
        }

        var result = mockMvc.perform(get("/api/characters")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<CharacterFormDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getContent().size()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    void shouldSortCharactersByNameAsc() throws Exception {
        characterRepository.deleteAll();

        characterRepository.save(Character.builder()
                .name("Yoda")
                .swapiId(9999)
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://...")
                .build());

        characterRepository.save(Character.builder()
                .name("Anakin")
                .swapiId(8888)
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://...")
                .build());

        var result = mockMvc.perform(get("/api/characters")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();
        PageResponse<CharacterFormDTO> page = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(page.getContent().getFirst().getName()).isEqualTo("Anakin");
    }

    @Test
    void shouldHandleEmptyRelationsGracefully() throws Exception {
        characterRepository.save(Character.builder()
                .name("Lobot")
                .swapiId(55)
                .films(Set.of())
                .species(Set.of())
                .vehicles(Set.of())
                .starships(Set.of())
                .created(OffsetDateTime.now())
                .edited(OffsetDateTime.now())
                .url("https://swapi.dev/api/people/55/")
                .build());

        mockMvc.perform(get("/api/characters")
                        .param("search", "Lobot")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].films").isArray())
                .andExpect(jsonPath("$.content[0].species").isArray())
                .andExpect(jsonPath("$.content[0].vehicles").isArray())
                .andExpect(jsonPath("$.content[0].starships").isArray());
    }
}
