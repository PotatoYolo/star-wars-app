package com.starwars.backend.dto.form;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterFormDTO {
    private Long id;
    private String name;
    private String birthYear;
    private String gender;
    private String height;
    private String mass;
    private String hairColor;
    private String skinColor;
    private String eyeColor;
    private String homeworld;
    private Long homeworldId;
    private List<String> films;
    private Set<Long> filmIds;
    private List<String> species;
    private Set<Long> speciesIds;
    private List<String> vehicles;
    private Set<Long> vehicleIds;
    private List<String> starships;
    private Set<Long> starshipIds;
    private OffsetDateTime created;
    private OffsetDateTime edited;
    private String url;
}
