package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    Optional<Film> findBySwapiId(Integer swapiId);

    Page<Film> findByTitleContainingIgnoreCase(String name, Pageable pageable);
}
