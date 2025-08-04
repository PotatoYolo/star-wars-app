package com.starwars.backend.service;

import com.starwars.backend.dto.form.CharacterFormDTO;
import com.starwars.backend.exception.*;
import com.starwars.backend.mapper.CharacterMapper;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.*;
import com.starwars.backend.persisntence.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final PlanetRepository planetRepository;
    private final FilmRepository filmRepository;
    private final SpeciesRepository speciesRepository;
    private final VehicleRepository vehicleRepository;
    private final StarshipRepository starshipRepository;

    private final CharacterMapper characterMapper;

    @Transactional(readOnly = true)
    public Page<CharacterFormDTO> getCharacters(String search, Pageable pageable) {
        try {
            Pageable fixedPageable = PageRequest.of(pageable.getPageNumber(), 15, pageable.getSort());

            Page<Character> characters;
            if (search != null && !search.isBlank()) {
                log.info("Searching characters with name containing '{}'", search);
                characters = characterRepository.findByNameContainingIgnoreCase(search, fixedPageable);
            } else {
                log.info("Fetching all characters without search filter");
                characters = characterRepository.findAll(fixedPageable);
            }

            return characters.map(characterMapper::toDto);
        } catch (Exception ex) {
            log.error("Error while retrieving characters from database", ex);
            throw new CharacterRetrievalException("Unable to retrieve characters", ex);
        }
    }

    @Transactional
    public Character createCharacter(CharacterFormDTO dto) {
        try {
            log.info("Creating new character: {}", dto.getName());
            Character character = characterMapper.toEntity(dto);
            character.setCreated(OffsetDateTime.now());

            if (character.getSwapiId() == null) {
                int nextSwapiId = getNextSwapiId().intValue();
                character.setSwapiId(nextSwapiId);
                log.info("Assigned new SWAPI ID: {}", nextSwapiId);
            }

            fillRelations(character, dto);
            Character saved = characterRepository.save(character);
            log.info("Character saved with ID: {}", saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("Failed to create character", ex);
            throw new CharacterCreationException("Unable to create character", ex);
        }
    }

    @Transactional
    public Character updateCharacter(Long id, CharacterFormDTO dto) {
        try {
            log.info("Updating character with ID: {}", id);
            Character character = characterRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Character with ID {} not found", id);
                        return new CharacterNotFoundException("Character with ID " + id + " not found");
                    });

            Character updated = characterMapper.toEntity(dto);
            updated.setId(character.getId());
            updated.setSwapiId(character.getSwapiId());
            updated.setCreated(character.getCreated());
            updated.setEdited(OffsetDateTime.now());

            fillRelations(updated, dto);
            Character saved = characterRepository.save(updated);
            log.info("Character updated with ID: {}", saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("Failed to update character", ex);
            throw new CharacterUpdateException("Unable to update character", ex);
        }
    }

    @Transactional
    public void deleteCharacter(Long id) {
        try {
            log.info("Deleting character with ID: {}", id);
            if (!characterRepository.existsById(id)) {
                log.error("Character with ID {} not found", id);
                throw new CharacterNotFoundException("Character with ID " + id + " not found");
            }
            characterRepository.deleteById(id);
            log.info("Character deleted successfully");
        } catch (Exception ex) {
            log.error("Failed to delete character", ex);
            throw new CharacterDeletionException("Unable to delete character", ex);
        }
    }

    private void fillRelations(Character character, CharacterFormDTO dto) {
        if (dto.getHomeworldId() != null) {
            planetRepository.findById(dto.getHomeworldId()).ifPresentOrElse(
                    character::setHomeworld,
                    () -> log.warn("Homeworld with ID {} not found", dto.getHomeworldId())
            );
        } else {
            character.setHomeworld(null);
        }

        character.setFilms(new HashSet<>(filmRepository.findAllById(dto.getFilmIds())));
        character.setSpecies(new HashSet<>(speciesRepository.findAllById(dto.getSpeciesIds())));
        character.setVehicles(new HashSet<>(vehicleRepository.findAllById(dto.getVehicleIds())));
        character.setStarships(new HashSet<>(starshipRepository.findAllById(dto.getStarshipIds())));

        log.info("Relations filled for character '{}'", character.getName());
    }

    public CharacterFormDTO toDto(Character character) {
        return characterMapper.toDto(character);
    }

    public Long getNextSwapiId() {
        Long max = characterRepository.findMaxSwapiId();
        Long next = (max != null ? max : 9999L) + 1;
        log.debug("Next SWAPI ID calculated: {}", next);
        return next;
    }

    public Map<String, Object> getSupportData() {
        Map<String, Object> response = new HashMap<>();

        response.put("films", filmRepository.findAll().stream()
                .filter(f -> f.getId() != null && f.getTitle() != null)
                .map(f -> Map.of("id", f.getId(), "title", f.getTitle()))
                .toList());

        response.put("species", speciesRepository.findAll().stream()
                .filter(s -> s.getId() != null && s.getName() != null)
                .map(s -> Map.of("id", s.getId(), "name", s.getName()))
                .toList());

        response.put("vehicles", vehicleRepository.findAll().stream()
                .filter(v -> v.getId() != null && v.getName() != null)
                .map(v -> Map.of("id", v.getId(), "name", v.getName()))
                .toList());

        response.put("starships", starshipRepository.findAll().stream()
                .filter(s -> s.getId() != null && s.getName() != null)
                .map(s -> Map.of("id", s.getId(), "name", s.getName()))
                .toList());

        response.put("planets", planetRepository.findAll().stream()
                .filter(p -> p.getId() != null && p.getName() != null)
                .map(p -> Map.of("id", p.getId(), "name", p.getName()))
                .toList());

        return response;
    }
}