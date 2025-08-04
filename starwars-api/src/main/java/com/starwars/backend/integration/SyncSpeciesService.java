package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.SpeciesRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncSpeciesService {

    private final RestTemplate restTemplate;
    private final SpeciesRepository speciesRepository;
    private final CharacterRepository characterRepository;
    private final FilmRepository filmRepository;

    @Transactional
    public void syncSpecies() {
        log.info("Starting species sync from SWAPI...");
        String url = Utils.URL_SPECIES;

        while (url != null) {
            try {
                List<SpeciesDTO> results = fetchSpeciesFromUrl(url);
                if (results != null) {
                    results.forEach(this::processSpecies);
                }
                url = getNextPageUrl(url);
            } catch (Exception e) {
                log.error("Error fetching species from SWAPI at URL {}: {}", url, e.getMessage(), e);
                url = null;
            }
        }

        log.info("Species sync completed.");
    }

    private List<SpeciesDTO> fetchSpeciesFromUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<SpeciesDTO>>() {}
        ).getBody();

        if (response.results() == null) {
            log.warn("No results found at URL: {}", currentUrl);
            return Collections.emptyList();
        }

        return response.results();
    }

    private String getNextPageUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<SpeciesDTO>>() {}
        ).getBody();

        return Optional.of(response)
                .map(SwapiResponse::next)
                .orElse(null);
    }

    private void processSpecies(SpeciesDTO dto) {
        Integer swapiId = Utils.extractSwapiId(dto.url());
        if (swapiId == null) {
            log.warn("Skipping species with null swapiId, url: {}", dto.url());
            return;
        }

        Species species = speciesRepository.findBySwapiId(swapiId)
                .orElseGet(() -> mapToSpeciesEntity(dto, swapiId));

        speciesRepository.saveAndFlush(species);

        species.getCharacters().clear();
        species.getFilms().clear();

        for (String characterUrl : dto.people()) {
            Integer charId = Utils.extractSwapiId(characterUrl);
            characterRepository.findBySwapiId(charId).ifPresent(character -> {
                species.getCharacters().add(character);
                if (character.getSpecies() == null) character.setSpecies(new HashSet<>());
                character.getSpecies().add(species);
            });
        }

        for (String filmUrl : dto.films()) {
            Integer filmId = Utils.extractSwapiId(filmUrl);
            filmRepository.findBySwapiId(filmId).ifPresent(film -> {
                species.getFilms().add(film);
                if (film.getSpecies() == null) film.setSpecies(new HashSet<>());
                film.getSpecies().add(species);
            });
        }

        speciesRepository.save(species);
        log.info("Species saved/updated (swapiId={})", swapiId);
    }


    private Species mapToSpeciesEntity(SpeciesDTO dto, Integer swapiId) {
        return Species.builder()
                .swapiId(swapiId)
                .name(dto.name())
                .classification(dto.classification())
                .designation(dto.designation())
                .averageHeight(dto.averageHeight())
                .skinColors(dto.skinColors())
                .hairColors(dto.hairColors())
                .eyeColors(dto.eyeColors())
                .averageLifespan(dto.averageLifespan())
                .language(dto.language())
                .created(dto.created())
                .edited(dto.edited())
                .url(dto.url())
                .homeworld(dto.homeworld())
                .characters(new HashSet<>())
                .films(new HashSet<>())
                .build();
    }
}
