package com.starwars.backend.integration;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.SwapiResponse;
import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.persisntence.entities.Vehicle;
import com.starwars.backend.persisntence.repository.CharacterRepository;
import com.starwars.backend.persisntence.repository.FilmRepository;
import com.starwars.backend.persisntence.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncVehicleService {

    private final RestTemplate restTemplate;
    private final VehicleRepository vehicleRepository;
    private final CharacterRepository characterRepository;
    private final FilmRepository filmRepository;

    @Transactional
    public void syncVehicles() {
        log.info("Starting vehicle sync from SWAPI...");
        String url = Utils.URL_VEHICLES;

        while (url != null) {
            try {
                List<VehicleDTO> results = fetchVehiclesFromUrl(url);
                results.forEach(this::processVehicle);
                url = getNextPageUrl(url);
            } catch (Exception e) {
                log.error("Error fetching vehicles from SWAPI at URL {}: {}", url, e.getMessage(), e);
                break;
            }
        }

        log.info("Vehicle sync completed.");
    }

    private List<VehicleDTO> fetchVehiclesFromUrl(String url) {
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<VehicleDTO>>() {}
        ).getBody();

        if (response.results() == null) {
            log.warn("Response received without 'results' field at URL: {}", url);
            return Collections.emptyList();
        }

        return response.results();
    }

    private String getNextPageUrl(String currentUrl) {
        var response = restTemplate.exchange(
                currentUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<SwapiResponse<VehicleDTO>>() {}
        ).getBody();

        return Optional.of(response)
                .map(SwapiResponse::next)
                .orElse(null);
    }

    private void processVehicle(VehicleDTO dto) {
        Integer swapiId = Utils.extractSwapiId(dto.url());
        if (swapiId == null) {
            log.warn("Skipping vehicle with null swapiId, url: {}", dto.url());
            return;
        }

        Vehicle vehicle = vehicleRepository.findBySwapiId(swapiId)
                .orElseGet(() -> {
                    Vehicle v = mapToVehicleEntity(dto, swapiId);
                    v.setFilms(new HashSet<>());
                    v.setPilots(new HashSet<>());
                    return v;
                });

        vehicle.getFilms().clear();
        vehicle.getPilots().clear();

        mapFilms(vehicle, dto);
        mapPilots(vehicle, dto);

        vehicleRepository.save(vehicle);
        log.info("Vehicle saved/updated (swapiId={}): {}", swapiId, vehicle.getName());
    }

    private void mapFilms(Vehicle vehicle, VehicleDTO dto) {
        for (String filmUrl : dto.films()) {
            try {
                Integer filmId = Utils.extractSwapiId(filmUrl);
                filmRepository.findBySwapiId(filmId).ifPresent(film -> {
                    vehicle.getFilms().add(film);
                    film.getVehicles().add(vehicle);
                });
            } catch (Exception e) {
                log.error("Error mapping film {} to vehicle {}: {}", filmUrl, vehicle.getSwapiId(), e.getMessage());
            }
        }
    }

    private void mapPilots(Vehicle vehicle, VehicleDTO dto) {
        for (String pilotUrl : dto.pilots()) {
            try {
                Integer pilotId = Utils.extractSwapiId(pilotUrl);
                characterRepository.findBySwapiId(pilotId).ifPresent(pilot -> {
                    vehicle.getPilots().add(pilot);
                    pilot.getVehicles().add(vehicle);
                });
            } catch (Exception e) {
                log.error("Error mapping pilot {} to vehicle {}: {}", pilotUrl, vehicle.getSwapiId(), e.getMessage());
            }
        }
    }

    private Vehicle mapToVehicleEntity(VehicleDTO dto, Integer swapiId) {
        return Vehicle.builder()
                .swapiId(swapiId)
                .name(dto.name())
                .model(dto.model())
                .manufacturer(dto.manufacturer())
                .costInCredits(dto.costInCredits())
                .length(dto.length())
                .maxAtmospheringSpeed(dto.maxAtmospheringSpeed())
                .crew(dto.crew())
                .passengers(dto.passengers())
                .cargoCapacity(dto.cargoCapacity())
                .consumables(dto.consumables())
                .vehicleClass(dto.vehicleClass())
                .created(dto.created())
                .edited(dto.edited())
                .url(dto.url())
                .films(new HashSet<>())
                .pilots(new HashSet<>())
                .build();
    }
}
