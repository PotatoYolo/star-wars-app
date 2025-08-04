package com.starwars.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record FilmDTO(
        String title,

        @JsonProperty("episode_id")
        Integer episodeId,

        @JsonProperty("opening_crawl")
        String openingCrawl,

        String director,
        String producer,

        @JsonProperty("release_date")
        String releaseDate,

        List<String> characters,
        List<String> planets,
        List<String> starships,
        List<String> vehicles,
        List<String> species,

        OffsetDateTime created,
        OffsetDateTime edited,
        String url
) {}
