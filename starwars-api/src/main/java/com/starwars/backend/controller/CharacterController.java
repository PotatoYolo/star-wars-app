package com.starwars.backend.controller;

import com.starwars.backend.dto.form.CharacterFormDTO;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Slf4j
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping
    public ResponseEntity<Page<CharacterFormDTO>> getCharacters(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        try {
            Page<CharacterFormDTO> result = characterService.getCharacters(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch characters: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<CharacterFormDTO> createCharacter(@RequestBody CharacterFormDTO dto) {
        try {
            CharacterFormDTO result = characterService.createCharacter(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Failed to create character: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterFormDTO> updateCharacter(@PathVariable Long id, @RequestBody CharacterFormDTO dto) {
        try {
            Character updated = characterService.updateCharacter(id, dto);
            CharacterFormDTO result = characterService.toDto(updated);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to update character {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        try {
            characterService.deleteCharacter(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete character {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/support-data")
    public Map<String, Object> getCharacterSupportData() {
        try {
            return characterService.getSupportData();
        } catch (Exception e) {
            log.error("Failed to load support data: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}