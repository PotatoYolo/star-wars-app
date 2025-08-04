package com.starwars.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record PlanetDTO(
        String name,

        @JsonProperty("rotation_period")
        String rotationPeriod,

        @JsonProperty("orbital_period")
        String orbitalPeriod,

        String diameter,
        String climate,
        String gravity,
        String terrain,

        @JsonProperty("surface_water")
        String surfaceWater,

        String population,
        List<String> residents,
        List<String> films,
        OffsetDateTime created,
        OffsetDateTime edited,
        String url
) {}
