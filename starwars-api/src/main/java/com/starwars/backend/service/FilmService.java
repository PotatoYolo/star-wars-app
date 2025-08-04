package com.starwars.backend.service;

import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.exception.FilmRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private final FilmRepository filmRepository;

    @Transactional(readOnly = true)
    public Page<FilmDTO> getFilms(String search, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                15,
                pageable.getSort()
        );

        try {
            Page<Film> films;

            if (search != null && !search.isBlank()) {
                films = filmRepository.findByTitleContainingIgnoreCase(search, fixedPageable);
            } else {
                films = filmRepository.findAll(fixedPageable);
            }

            return films.map(film -> new FilmDTO(
                    film.getTitle(),
                    film.getEpisodeId(),
                    film.getOpeningCrawl(),
                    film.getDirector(),
                    film.getProducer(),
                    film.getReleaseDate() != null ? film.getReleaseDate().toLocalDate().toString() : null,
                    film.getCharacters().stream().map(Character::getName).toList(),
                    film.getPlanets().stream().map(Planet::getName).toList(),
                    film.getStarships().stream().map(Starship::getName).toList(),
                    film.getVehicles().stream().map(Vehicle::getName).toList(),
                    film.getSpecies().stream().map(Species::getName).toList(),
                    film.getCreated(),
                    film.getEdited(),
                    film.getUrl()
            ));
        } catch (Exception ex) {
            log.error("Failed to retrieve films from the database", ex);
            throw new FilmRetrievalException("Unable to retrieve films at this time", ex);
        }
    }
}
