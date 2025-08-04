package com.starwars.backend.service;

import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.exception.PlanetRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.repository.PlanetRepository;
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
public class PlanetService {

    private final PlanetRepository planetRepository;

    @Transactional(readOnly = true)
    public Page<PlanetDTO> getPlanets(String search, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                15,
                pageable.getSort()
        );

        try {
            Page<Planet> planets;

            if (search != null && !search.isBlank()) {
                planets = planetRepository.findByNameContainingIgnoreCase(search, fixedPageable);
            } else {
                planets = planetRepository.findAll(fixedPageable);
            }

            return planets.map(planet -> new PlanetDTO(
                    planet.getName(),
                    planet.getRotationPeriod(),
                    planet.getOrbitalPeriod(),
                    planet.getDiameter(),
                    planet.getClimate(),
                    planet.getGravity(),
                    planet.getTerrain(),
                    planet.getSurfaceWater(),
                    planet.getPopulation(),
                    planet.getResidents().stream().map(Character::getName).toList(),
                    planet.getFilms().stream().map(Film::getTitle).toList(),
                    planet.getCreated(),
                    planet.getEdited(),
                    planet.getUrl()
            ));
        } catch (Exception ex) {
            log.error("Failed to fetch planets from the database", ex);
            throw new PlanetRetrievalException("Unable to retrieve planets at this time", ex);
        }
    }
}
