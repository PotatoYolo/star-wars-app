package com.starwars.backend.controller;

import com.starwars.backend.dto.VehicleDTO;
import com.starwars.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<Page<VehicleDTO>> listVehicles(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            Page<VehicleDTO> result = vehicleService.getVehicles(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch vehicles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}