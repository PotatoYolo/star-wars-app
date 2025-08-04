package com.starwars.backend.persisntence.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer swapiId;

    private String name;

    private String birthYear;

    private String gender;

    private String height;

    private String mass;

    private String hairColor;

    private String skinColor;

    private String eyeColor;

    @ManyToOne
    @JoinColumn(name = "planet_id")
    @JsonBackReference
    private Planet homeworld;

    @ManyToMany
    @JoinTable(
            name = "character_films",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    @JsonManagedReference
    private Set<Film> films = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "character_species",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "species_id")
    )
    @JsonManagedReference
    private Set<Species> species = new HashSet<>();

    @ManyToMany(mappedBy = "pilots")
    private Set<Vehicle> vehicles = new HashSet<>();

    @ManyToMany(mappedBy = "pilots")
    private Set<Starship> starships = new HashSet<>();

    private OffsetDateTime created;

    private OffsetDateTime edited;

    private String url;
}
