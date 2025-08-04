package com.starwars.backend.persisntence.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "films")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer swapiId;

    @Column(length = 255)
    private String title;

    private Integer episodeId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String openingCrawl;

    private String director;

    private String producer;

    private OffsetDateTime releaseDate;

    private OffsetDateTime created;

    private OffsetDateTime edited;

    private String url;

    @ManyToMany(mappedBy = "films")
    @JsonBackReference
    private Set<Character> characters = new HashSet<>();

    @ManyToMany(mappedBy = "films")
    @JsonBackReference
    private Set<Vehicle> vehicles = new HashSet<>();

    @ManyToMany(mappedBy = "films")
    @JsonBackReference
    private Set<Starship> starships = new HashSet<>();

    @ManyToMany(mappedBy = "films")
    @JsonBackReference
    private Set<Species> species = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "planet_films",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "planet_id")
    )
    @JsonManagedReference
    private Set<Planet> planets = new HashSet<>();
}
