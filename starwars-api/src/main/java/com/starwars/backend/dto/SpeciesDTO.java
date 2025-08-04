package com.starwars.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record SpeciesDTO(
        String name,
        String classification,
        String designation,

        @JsonProperty("average_height")
        String averageHeight,

        @JsonProperty("skin_colors")
        String skinColors,

        @JsonProperty("hair_colors")
        String hairColors,

        @JsonProperty("eye_colors")
        String eyeColors,

        @JsonProperty("average_lifespan")
        String averageLifespan,

        String language,
        List<String> people,
        List<String> films,
        OffsetDateTime created,
        OffsetDateTime edited,
        String url,

        String homeworld
) {}
