package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.CharacterDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.persisntence.entities.*;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncCharacterService {

    private final RestTemplate restTemplate;
    private final CharacterRepository characterRepository;
    private final PlanetRepository planetRepository;
    private final FilmRepository filmRepository;
    private final SpeciesRepository speciesRepository;
    private final VehicleRepository vehicleRepository;
    private final StarshipRepository starshipRepository;

    @Transactional
    public void syncCharacters() {
        log.info("Starting character sync from SWAPI...");
        String url = Utils.URL_PEOPLE;

        boolean errorOccurred = false;

        while (url != null && !errorOccurred) {
            try {
                var response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<SwapiResponse<CharacterDTO>>() {}
                ).getBody();

                var results = response.results();

                if (results != null) {
                    for (CharacterDTO dto : results) {
                        Integer swapiId = Utils.extractSwapiId(dto.url());

                        if (swapiId != null) {
                            Character character = characterRepository.findBySwapiId(swapiId)
                                    .orElseGet(Character::new);

                            character.setSwapiId(swapiId);
                            mapBasicFields(character, dto);
                            mapRelations(character, dto);

                            characterRepository.save(character);
                            log.info("Character saved/updated (swapiId={})", swapiId);
                        } else {
                            log.warn("Skipping character with null swapiId, url: {}", dto.url());
                        }
                    }

                    url = response.next();
                } else {
                    log.warn("Response received without 'results' field at URL: {}", url);
                    url = null;
                }

            } catch (Exception e) {
                log.error("Error fetching characters from SWAPI at URL {}: {}", url, e.getMessage(), e);
                errorOccurred = true;
            }
        }

        log.info("Character sync completed.");
    }

    private void mapBasicFields(Character character, CharacterDTO dto) {
        character.setName(dto.name());
        character.setBirthYear(dto.birthYear());
        character.setGender(dto.gender());
        character.setHeight(dto.height());
        character.setMass(dto.mass());
        character.setHairColor(dto.hairColor());
        character.setSkinColor(dto.skinColor());
        character.setEyeColor(dto.eyeColor());
        character.setCreated(dto.created());
        character.setEdited(dto.edited());
        character.setUrl(dto.url());

        if (dto.homeworld() != null && !dto.homeworld().isBlank()) {
            try {
                Integer planetId = Utils.extractSwapiId(dto.homeworld());
                planetRepository.findBySwapiId(planetId).ifPresent(character::setHomeworld);
            } catch (Exception ex) {
                log.error("Error mapping homeworld for character '{}': {}", dto.name(), ex.getMessage());
            }
        }
    }

    private void mapRelations(Character character, CharacterDTO dto) {
        mapFilms(character, dto);
        mapSpecies(character, dto);
        mapVehicles(character, dto);
        mapStarships(character, dto);
    }

    private void mapFilms(Character character, CharacterDTO dto) {
        character.getFilms().clear();
        if (dto.films() != null) {
            for (String filmUrl : dto.films()) {
                try {
                    Integer filmId = Utils.extractSwapiId(filmUrl);
                    filmRepository.findBySwapiId(filmId).ifPresent(film -> {
                        character.getFilms().add(film);
                        film.getCharacters().add(character);
                        log.debug("Mapped film '{}' to character '{}'", film.getTitle(), character.getName());
                    });
                } catch (Exception ex) {
                    log.error("Error mapping film '{}' to character '{}': {}", filmUrl, character.getName(), ex.getMessage());
                }
            }
        }
    }

    private void mapSpecies(Character character, CharacterDTO dto) {
        character.getSpecies().clear();
        if (dto.species() != null) {
            for (String speciesUrl : dto.species()) {
                try {
                    Integer speciesId = Utils.extractSwapiId(speciesUrl);
                    speciesRepository.findBySwapiId(speciesId).ifPresent(species -> {
                        character.getSpecies().add(species);
                        species.getCharacters().add(character);
                        log.debug("Mapped species '{}' to character '{}'", species.getName(), character.getName());
                    });
                } catch (Exception ex) {
                    log.error("Error mapping species '{}' to character '{}': {}", speciesUrl, character.getName(), ex.getMessage());
                }
            }
        }
    }

    private void mapVehicles(Character character, CharacterDTO dto) {
        character.getVehicles().clear();
        if (dto.vehicles() != null) {
            for (String vehicleUrl : dto.vehicles()) {
                try {
                    Integer vehicleId = Utils.extractSwapiId(vehicleUrl);
                    vehicleRepository.findBySwapiId(vehicleId).ifPresent(vehicle -> {
                        character.getVehicles().add(vehicle);
                        vehicle.getPilots().add(character);
                        log.debug("Mapped vehicle '{}' to character '{}'", vehicle.getName(), character.getName());
                    });
                } catch (Exception ex) {
                    log.error("Error mapping vehicle '{}' to character '{}': {}", vehicleUrl, character.getName(), ex.getMessage());
                }
            }
        }
    }

    private void mapStarships(Character character, CharacterDTO dto) {
        character.getStarships().clear();
        if (dto.starships() != null) {
            for (String starshipUrl : dto.starships()) {
                try {
                    Integer starshipId = Utils.extractSwapiId(starshipUrl);
                    starshipRepository.findBySwapiId(starshipId).ifPresent(starship -> {
                        character.getStarships().add(starship);
                        starship.getPilots().add(character);
                        log.debug("Mapped starship '{}' to character '{}'", starship.getName(), character.getName());
                    });
                } catch (Exception ex) {
                    log.error("Error mapping starship '{}' to character '{}': {}", starshipUrl, character.getName(), ex.getMessage());
                }
            }
        }
    }
}
