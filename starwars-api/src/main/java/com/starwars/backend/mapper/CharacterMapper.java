package com.starwars.backend.mapper;

import com.starwars.backend.dto.form.CharacterFormDTO;
import com.starwars.backend.persisntence.entities.*;
import com.starwars.backend.persisntence.entities.Character;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CharacterMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "birthYear", target = "birthYear"),
            @Mapping(source = "gender", target = "gender"),
            @Mapping(source = "height", target = "height"),
            @Mapping(source = "mass", target = "mass"),
            @Mapping(source = "hairColor", target = "hairColor"),
            @Mapping(source = "skinColor", target = "skinColor"),
            @Mapping(source = "eyeColor", target = "eyeColor"),
            @Mapping(source = "homeworld.id", target = "homeworldId"),
            @Mapping(source = "homeworld.name", target = "homeworld"),
            @Mapping(source = "films", target = "films"),
            @Mapping(source = "films", target = "filmIds"),
            @Mapping(source = "species", target = "species"),
            @Mapping(source = "species", target = "speciesIds"),
            @Mapping(source = "vehicles", target = "vehicles"),
            @Mapping(source = "vehicles", target = "vehicleIds"),
            @Mapping(source = "starships", target = "starships"),
            @Mapping(source = "starships", target = "starshipIds"),
            @Mapping(source = "created", target = "created"),
            @Mapping(source = "edited", target = "edited"),
            @Mapping(source = "url", target = "url")
    })
    CharacterFormDTO toDto(Character character);

    @Mappings({
            @Mapping(target = "homeworld", ignore = true),
            @Mapping(target = "films", ignore = true),
            @Mapping(target = "species", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "starships", ignore = true),
            @Mapping(target = "created", ignore = true),
            @Mapping(target = "edited", ignore = true),
            @Mapping(target = "swapiId", ignore = true)
    })
    Character toEntity(CharacterFormDTO dto);

    default Set<Long> mapFilmIds(Set<Film> films) {
        return films.stream().map(Film::getId).collect(Collectors.toSet());
    }

    default List<String> mapFilms(Set<Film> films) {
        return films.stream().map(Film::getTitle).toList();
    }

    default Set<Long> mapSpeciesIds(Set<Species> species) {
        return species.stream().map(Species::getId).collect(Collectors.toSet());
    }

    default List<String> mapSpecies(Set<Species> species) {
        return species.stream().map(Species::getName).toList();
    }

    default Set<Long> mapVehicleIds(Set<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getId).collect(Collectors.toSet());
    }

    default List<String> mapVehicles(Set<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getName).toList();
    }

    default Set<Long> mapStarshipIds(Set<Starship> starships) {
        return starships.stream().map(Starship::getId).collect(Collectors.toSet());
    }

    default List<String> mapStarships(Set<Starship> starships) {
        return starships.stream().map(Starship::getName).toList();
    }
}
