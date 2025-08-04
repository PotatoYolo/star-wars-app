package com.starwars.backend;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    public static final String URL_PLANETS = "https://swapi.dev/api/planets";
    public static final String URL_PEOPLE = "https://swapi.dev/api/people";
    public static final String URL_FILMS = "https://swapi.dev/api/films/";
    public static final String URL_SPECIES = "https://swapi.dev/api/species/";
    public static final String URL_STARSHIP = "https://swapi.dev/api/starships/";
    public static final String URL_VEHICLES = "https://swapi.dev/api/vehicles/";


    public static Integer extractSwapiId(String url) {
        if (url == null || url.isBlank()) return null;
        String[] parts = url.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isBlank()) {
                try {
                    return Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
