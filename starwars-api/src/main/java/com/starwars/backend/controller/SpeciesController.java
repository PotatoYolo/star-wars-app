package com.starwars.backend.controller;

import com.starwars.backend.dto.SpeciesDTO;
import com.starwars.backend.service.SpeciesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/species")
@Slf4j
public class SpeciesController {

    private final SpeciesService speciesService;

    @GetMapping
    public ResponseEntity<Page<SpeciesDTO>> listSpecies(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            Page<SpeciesDTO> result = speciesService.getSpecies(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch species: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}