package com.starwars.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record StarshipDTO(
        String name,
        String model,
        String manufacturer,

        @JsonProperty("cost_in_credits")
        String costInCredits,

        String length,

        @JsonProperty("max_atmosphering_speed")
        String maxAtmospheringSpeed,

        String crew,
        String passengers,

        @JsonProperty("cargo_capacity")
        String cargoCapacity,

        String consumables,

        @JsonProperty("hyperdrive_rating")
        String hyperdriveRating,

        @JsonProperty("MGLT")
        String mglt,

        @JsonProperty("starship_class")
        String starshipClass,

        List<String> pilots,
        List<String> films,
        OffsetDateTime created,
        OffsetDateTime edited,
        String url
) {}
