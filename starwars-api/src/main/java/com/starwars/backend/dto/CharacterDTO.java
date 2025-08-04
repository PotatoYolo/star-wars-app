package com.starwars.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record CharacterDTO(
        String name,

        @JsonProperty("birth_year")
        String birthYear,

        String gender,
        String height,
        String mass,

        @JsonProperty("hair_color")
        String hairColor,

        @JsonProperty("skin_color")
        String skinColor,

        @JsonProperty("eye_color")
        String eyeColor,

        String homeworld,
        List<String> films,
        List<String> species,
        List<String> vehicles,
        List<String> starships,
        OffsetDateTime created,
        OffsetDateTime edited,
        String url
) {}
