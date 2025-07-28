package com.starwars.backend.controller;

import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.service.PlanetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planets")
public class PlanetController {

    private final PlanetService service;

    public PlanetController(PlanetService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PlanetDTO> getPlanets(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return service.getPlanets(pageable, search);
    }
}
