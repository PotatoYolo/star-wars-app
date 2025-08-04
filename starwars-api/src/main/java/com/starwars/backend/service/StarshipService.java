package com.starwars.backend.service;

import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.exception.StarshipRetrievalException; // ✅ IMPORT: tu excepción personalizada
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Starship;
import com.starwars.backend.persisntence.repository.StarshipRepository;
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
public class StarshipService {

    private final StarshipRepository starshipRepository;

    @Transactional(readOnly = true)
    public Page<StarshipDTO> getStarships(String search, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                15,
                pageable.getSort()
        );

        try {
            Page<Starship> starships;
            if (search != null && !search.isBlank()) {
                starships = starshipRepository.findByNameContainingIgnoreCase(search, fixedPageable);
            } else {
                starships = starshipRepository.findAll(fixedPageable);
            }

            return starships.map(s -> {
                log.info("Starship: {}", s.getName());
                log.info("Films: {}", s.getFilms().stream().map(Film::getTitle).toList());
                log.info("Pilots: {}", s.getPilots().stream().map(Character::getName).toList());

                return new StarshipDTO(
                        s.getName(),
                        s.getModel(),
                        s.getManufacturer(),
                        s.getCostInCredits(),
                        s.getLength(),
                        s.getMaxAtmospheringSpeed(),
                        s.getCrew(),
                        s.getPassengers(),
                        s.getCargoCapacity(),
                        s.getConsumables(),
                        s.getHyperdriveRating(),
                        s.getMglt(),
                        s.getStarshipClass(),
                        s.getPilots().stream().map(Character::getName).toList(),
                        s.getFilms().stream().map(Film::getTitle).toList(),
                        s.getCreated(),
                        s.getEdited(),
                        s.getUrl()
                );
            });
        } catch (Exception e) {
            log.error("Failed to retrieve starships: {}", e.getMessage(), e);
            throw new StarshipRetrievalException("Error retrieving starships", e);
        }
    }
}
