package com.starwars.backend.service;

import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.exception.VehicleRetrievalException;
import com.starwars.backend.persisntence.entities.Character;
import com.starwars.backend.persisntence.entities.Film;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.VehicleRepository;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Page<VehicleDTO> getVehicles(String search, Pageable pageable) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                15,
                pageable.getSort()
        );

        try {
            Page<Vehicle> vehicles;
            if (search != null && !search.isBlank()) {
                vehicles = vehicleRepository.findByNameContainingIgnoreCase(search, fixedPageable);
            } else {
                vehicles = vehicleRepository.findAll(fixedPageable);
            }

            return vehicles.map(v -> new VehicleDTO(
                    v.getName(),
                    v.getModel(),
                    v.getManufacturer(),
                    v.getCostInCredits(),
                    v.getVehicleClass(),
                    v.getLength(),
                    v.getMaxAtmospheringSpeed(),
                    v.getCrew(),
                    v.getPassengers(),
                    v.getCargoCapacity(),
                    v.getConsumables(),
                    v.getPilots().stream().map(Character::getName).toList(),
                    v.getFilms().stream().map(Film::getTitle).toList(),
                    v.getCreated(),
                    v.getEdited(),
                    v.getUrl()
            ));
        } catch (Exception ex) {
            log.error("Failed to retrieve vehicles from the database", ex);
            throw new VehicleRetrievalException("Unable to retrieve vehicles", ex);
        }
    }
}
