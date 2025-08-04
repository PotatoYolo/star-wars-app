package com.starwars.backend.dto;

import java.util.List;

public record SwapiResponse<T>(
        int count,
        String next,
        String previous,
        List<T> results
) {}
