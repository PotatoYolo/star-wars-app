package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Starship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StarshipRepository extends JpaRepository<Starship, Long> {

    Page<Starship> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Starship> findAll(Pageable pageable);

    Optional<Starship> findBySwapiId(Integer swapiId);
}
