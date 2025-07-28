package com.starwars.backend.dto;

import java.time.OffsetDateTime;

public record PlanetDTO(String name, String climate, String terrain, OffsetDateTime created, String url) {

}

