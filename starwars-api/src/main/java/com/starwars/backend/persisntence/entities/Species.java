package com.starwars.backend.persisntence.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "species")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer swapiId;

    private String name;

    private String classification;

    private String designation;

    private String averageHeight;

    private String skinColors;

    private String hairColors;

    private String eyeColors;

    private String averageLifespan;

    private String language;

    private OffsetDateTime created;

    private OffsetDateTime edited;

    private String url;

    @Column(name = "homeworld")
    private String homeworld;

    @ManyToMany(mappedBy = "species")
    @JsonBackReference
    private Set<Character> characters = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "species_films",
            joinColumns = @JoinColumn(name = "species_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    @JsonManagedReference
    private Set<Film> films = new HashSet<>();

}
