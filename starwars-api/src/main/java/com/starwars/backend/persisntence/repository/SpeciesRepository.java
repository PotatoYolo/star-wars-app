package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, Long> {

    Page<Species> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Species> findBySwapiId(Integer swapiId);
}
