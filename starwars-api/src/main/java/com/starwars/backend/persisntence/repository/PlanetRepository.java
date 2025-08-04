package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Planet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanetRepository extends JpaRepository<Planet, Long> {

    Optional<Planet> findBySwapiId(Integer swapiId);

    Page<Planet> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
