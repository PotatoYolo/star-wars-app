package com.starwars.backend.service;

import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.exception.SpeciesRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Planet;
import com.starwars.backend.persisntence.entities.Species;
import com.starwars.backend.persisntence.repository.SpeciesRepository;
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
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    @Transactional(readOnly = true)
    public Page<SpeciesDTO> getSpecies(String search, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                15,
                pageable.getSort()
        );

        try {
            Page<Species> species;
            if (search != null && !search.isBlank()) {
                log.info("Searching species with name containing '{}'", search);
                species = speciesRepository.findByNameContainingIgnoreCase(search, fixedPageable);
            } else {
                log.info("Fetching all species without search filter");
                species = speciesRepository.findAll(fixedPageable);
            }

            return species.map(s -> {
                String homeworld = s.getCharacters().stream()
                        .map(Character::getHomeworld)
                        .filter(p -> p != null && p.getName() != null)
                        .map(Planet::getName)
                        .findFirst()
                        .orElse("Unknown");

                return new SpeciesDTO(
                        s.getName(),
                        s.getClassification(),
                        s.getDesignation(),
                        s.getAverageHeight(),
                        s.getSkinColors(),
                        s.getHairColors(),
                        s.getEyeColors(),
                        s.getAverageLifespan(),
                        s.getLanguage(),
                        s.getCharacters().stream().map(Character::getName).toList(),
                        s.getFilms().stream().map(Film::getTitle).toList(),
                        s.getCreated(),
                        s.getEdited(),
                        s.getUrl(),
                        homeworld
                );
            });

        } catch (Exception ex) {
            log.error("Failed to retrieve species from database", ex);
            throw new SpeciesRetrievalException("Unable to retrieve species", ex);
        }
    }
}
