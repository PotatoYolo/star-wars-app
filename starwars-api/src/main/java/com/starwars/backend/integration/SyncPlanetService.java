package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.PlanetRepository;
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
@Slf4j
@RequiredArgsConstructor
public class SyncPlanetService {

    private final RestTemplate restTemplate;
    private final PlanetRepository planetRepository;
    private final CharacterRepository characterRepository;
    private final FilmRepository filmRepository;

    @Transactional
    public void syncPlanets() {
        log.info("Starting planet sync from SWAPI...");
        String url = Utils.URL_PLANETS;

        while (url != null) {
            List<PlanetDTO> results = null;
            try {
                results = fetchPlanetsFromUrl(url);
                results.forEach(this::processPlanet);
                url = getNextPageUrl(url);
            } catch (Exception e) {
                log.error("Error fetching planets from SWAPI at URL {}: {}", url, e.getMessage(), e);
                url = null;
            }
        }

        log.info("Planet sync completed.");
    }

    private List<PlanetDTO> fetchPlanetsFromUrl(String url) {
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<PlanetDTO>>() {}
        ).getBody();

        if (response.results() == null) {
            log.warn("No results found at URL: {}", url);
            return Collections.emptyList();
        }

        return response.results();
    }

    private String getNextPageUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<PlanetDTO>>() {}
        ).getBody();

        return Optional.of(response)
                .map(SwapiResponse::next)
                .orElse(null);
    }

    private void processPlanet(PlanetDTO dto) {
        Integer swapiId = Utils.extractSwapiId(dto.url());
        if (swapiId == null) {
            log.warn("Skipping planet with null swapiId, url: {}", dto.url());
            return;
        }

        Planet planet = planetRepository.findBySwapiId(swapiId)
                .orElseGet(() -> mapToPlanetEntity(dto, swapiId));

        planet.getResidents().clear();
        planet.getFilms().clear();

        mapResidents(planet, dto);
        mapFilms(planet, dto);

        planetRepository.save(planet);
        log.info("Planet saved/updated (swapiId={})", swapiId);
    }

    private void mapResidents(Planet planet, PlanetDTO dto) {
        if (dto.residents() == null) return;

        for (String residentUrl : dto.residents()) {
            Integer residentId = Utils.extractSwapiId(residentUrl);
            if (residentId != null) {
                characterRepository.findBySwapiId(residentId).ifPresent(character -> {
                    planet.getResidents().add(character);
                    character.setHomeworld(planet);
                });
            }
        }
    }

    private void mapFilms(Planet planet, PlanetDTO dto) {
        if (dto.films() == null) return;

        for (String filmUrl : dto.films()) {
            Integer filmId = Utils.extractSwapiId(filmUrl);
            if (filmId != null) {
                filmRepository.findBySwapiId(filmId).ifPresent(film -> {
                    planet.getFilms().add(film);
                    film.getPlanets().add(planet);
                });
            }
        }
    }

    private Planet mapToPlanetEntity(PlanetDTO dto, Integer swapiId) {
        return Planet.builder()
                .swapiId(swapiId)
                .name(dto.name())
                .rotationPeriod(dto.rotationPeriod())
                .orbitalPeriod(dto.orbitalPeriod())
                .diameter(dto.diameter())
                .climate(dto.climate())
                .gravity(dto.gravity())
                .terrain(dto.terrain())
                .surfaceWater(dto.surfaceWater())
                .population(dto.population())
                .created(dto.created())
                .edited(dto.edited())
                .url(dto.url())
                .residents(new HashSet<>())
                .films(new HashSet<>())
                .build();
    }
}
