package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Character;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {

    Page<Character> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Character> findBySwapiId(Integer swapiId);

    @Query("SELECT MAX(c.swapiId) FROM Character c")
    Long findMaxSwapiId();
}