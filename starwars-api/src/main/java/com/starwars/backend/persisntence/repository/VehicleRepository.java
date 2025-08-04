package com.starwars.backend.persisntence.repository;

import com.starwars.backend.persisntence.entities.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findAll(Pageable pageable);

    Optional<Vehicle> findBySwapiId(Integer swapiId);

    Page<Vehicle> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
