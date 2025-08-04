package com.starwars.backend.controller;

import com.starwars.backend.dto.StarshipDTO;
import com.starwars.backend.service.StarshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/starships")
@Slf4j
public class StarshipController {

    private final StarshipService starshipService;

    @GetMapping
    public ResponseEntity<Page<StarshipDTO>> listStarships(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            Page<StarshipDTO> result = starshipService.getStarships(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch starships: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}