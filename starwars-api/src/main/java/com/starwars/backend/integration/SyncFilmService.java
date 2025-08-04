package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.PlanetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncFilmService {

    private final RestTemplate restTemplate;
    private final FilmRepository filmRepository;
    private final CharacterRepository characterRepository;
    private final PlanetRepository planetRepository;

    @Transactional
    public void syncFilms() {
        log.info("Starting film sync from SWAPI...");
        String url = Utils.URL_FILMS;

        while (url != null) {
            try {
                List<FilmDTO> results = fetchFilmsFromUrl(url);
                results.forEach(this::processFilmDTO);
                url = getNextPageUrl(url);
            } catch (Exception e) {
                log.error("Error fetching films from SWAPI at URL {}: {}", url, e.getMessage(), e);
                url = null;
            }
        }

        log.info("Film sync completed.");
    }

    private List<FilmDTO> fetchFilmsFromUrl(String url) {
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<FilmDTO>>() {}
        ).getBody();

        if (response.results() == null) {
            log.warn("No results found at URL: {}", url);
            return List.of();
        }

        return response.results();
    }

    private String getNextPageUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<FilmDTO>>() {}
        ).getBody();

        return Optional.of(response)
                .map(SwapiResponse::next)
                .orElse(null);
    }

    private void processFilmDTO(FilmDTO dto) {
        Integer swapiId = Utils.extractSwapiId(dto.url());

        if (swapiId == null) {
            log.warn("Skipping film with null swapiId, url: {}", dto.url());
            return;
        }

        Film film = filmRepository.findBySwapiId(swapiId)
                .orElseGet(() -> mapToFilmEntity(dto, swapiId));

        clearFilmRelations(film);
        mapCharactersToFilm(film, dto, swapiId);
        mapPlanetsToFilm(film, dto, swapiId);

        filmRepository.save(film);

        log.info("Film saved/updated (swapiId={})", swapiId);
    }

    private void clearFilmRelations(Film film) {
        if (film.getCharacters() == null) film.setCharacters(new HashSet<>());
        else film.getCharacters().clear();

        if (film.getPlanets() == null) film.setPlanets(new HashSet<>());
        else film.getPlanets().clear();
    }

    private void mapCharactersToFilm(Film film, FilmDTO dto, Integer swapiId) {
        for (String characterUrl : dto.characters()) {
            try {
                Integer charSwapiId = Utils.extractSwapiId(characterUrl);
                characterRepository.findBySwapiId(charSwapiId).ifPresent(character -> {
                    film.getCharacters().add(character);
                    if (character.getFilms() == null) character.setFilms(new HashSet<>());
                    character.getFilms().add(film);
                });
            } catch (Exception ex) {
                log.error("Error mapping character {} to film {}: {}", characterUrl, swapiId, ex.getMessage());
            }
        }
    }

    private void mapPlanetsToFilm(Film film, FilmDTO dto, Integer swapiId) {
        for (String planetUrl : dto.planets()) {
            try {
                Integer planetId = Utils.extractSwapiId(planetUrl);
                planetRepository.findBySwapiId(planetId).ifPresent(planet -> film.getPlanets().add(planet));
            } catch (Exception ex) {
                log.error("Error mapping planet {} to film {}: {}", planetUrl, swapiId, ex.getMessage());
            }
        }
    }

    private Film mapToFilmEntity(FilmDTO dto, Integer swapiId) {
        OffsetDateTime releaseDate = null;
        if (dto.releaseDate() != null) {
            try {
                LocalDate localDate = LocalDate.parse(dto.releaseDate());
                releaseDate = localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            } catch (Exception ex) {
                log.error("Failed to parse release date for film '{}': {}", dto.title(), ex.getMessage());
            }
        }

        return Film.builder()
                .swapiId(swapiId)
                .title(dto.title())
                .episodeId(dto.episodeId())
                .openingCrawl(dto.openingCrawl())
                .director(dto.director())
                .producer(dto.producer())
                .releaseDate(releaseDate)
                .created(dto.created())
                .edited(dto.edited())
                .url(dto.url())
                .characters(new HashSet<>())
                .planets(new HashSet<>())
                .build();
    }
}
