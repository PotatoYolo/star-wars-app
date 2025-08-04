package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.StarshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncStarshipService {

    private final RestTemplate restTemplate;
    private final StarshipRepository starshipRepository;
    private final CharacterRepository characterRepository;
    private final FilmRepository filmRepository;

    @Transactional
    public void syncStarships() {
        log.info("Starting starship sync from SWAPI...");
        String url = Utils.URL_STARSHIP;

        while (url != null) {
            try {
                List<StarshipDTO> results = fetchStarshipsFromUrl(url);
                if (results != null) {
                    results.forEach(this::processStarship);
                }
                url = getNextPageUrl(url);
            } catch (Exception e) {
                log.error("Error fetching starships from SWAPI at URL {}: {}", url, e.getMessage(), e);
                url = null;
            }
        }

        log.info("Starship sync completed.");
    }

    private List<StarshipDTO> fetchStarshipsFromUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<StarshipDTO>>() {}
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
                new ParameterizedTypeReference<SwapiResponse<StarshipDTO>>() {}
        ).getBody();

        return Optional.of(response)
                .map(SwapiResponse::next)
                .orElse(null);
    }

    private void processStarship(StarshipDTO dto) {
        Integer swapiId = Utils.extractSwapiId(dto.url());

        if (swapiId == null) {
            log.warn("Skipping starship with null swapiId, url: {}", dto.url());
            return;
        }

        Starship starship = starshipRepository.findBySwapiId(swapiId)
                .orElseGet(() -> mapToStarshipEntity(dto, swapiId));

        starship.getPilots().clear();
        starship.getFilms().clear();

        if (dto.pilots() != null) {
            for (String pilotUrl : dto.pilots()) {
                Integer pilotId = Utils.extractSwapiId(pilotUrl);
                characterRepository.findBySwapiId(pilotId).ifPresent(character -> {
                    starship.getPilots().add(character);
                    if (character.getStarships() == null) character.setStarships(new HashSet<>());
                    character.getStarships().add(starship);
                });
            }
        }

        if (dto.films() != null) {
            for (String filmUrl : dto.films()) {
                Integer filmId = Utils.extractSwapiId(filmUrl);
                filmRepository.findBySwapiId(filmId).ifPresent(film -> {
                    starship.getFilms().add(film);
                    if (film.getStarships() == null) film.setStarships(new HashSet<>());
                    film.getStarships().add(starship);
                });
            }
        }

        starshipRepository.save(starship);
        log.info("Starship saved/updated (swapiId={})", swapiId);
    }

    private Starship mapToStarshipEntity(StarshipDTO dto, Integer swapiId) {
        return Starship.builder()
                .swapiId(swapiId)
                .name(dto.name())
                .model(dto.model())
                .manufacturer(dto.manufacturer())
                .costInCredits(dto.costInCredits())
                .length(dto.length())
                .maxAtmospheringSpeed(dto.maxAtmospheringSpeed())
                .crew(dto.crew())
                .passengers(dto.passengers())
                .cargoCapacity(dto.cargoCapacity())
                .consumables(dto.consumables())
                .hyperdriveRating(dto.hyperdriveRating())
                .mglt(dto.mglt())
                .starshipClass(dto.starshipClass())
                .created(dto.created())
                .edited(dto.edited())
                .url(dto.url())
                .pilots(new HashSet<>())
                .films(new HashSet<>())
                .build();
    }

}
