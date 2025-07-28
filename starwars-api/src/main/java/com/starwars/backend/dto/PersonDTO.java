package com.starwars.backend.dto;

import java.time.OffsetDateTime;

public record PersonDTO(String name, String height, String mass, OffsetDateTime created, String url) {

}

