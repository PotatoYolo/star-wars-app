package com.starwars.backend.controller;

import com.starwars.backend.dto.FilmDTO;
import com.starwars.backend.service.FilmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<Page<FilmDTO>> listFilms(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            Page<FilmDTO> result = filmService.getFilms(search, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch films: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}