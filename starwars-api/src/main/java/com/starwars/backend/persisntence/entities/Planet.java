package com.starwars.backend.persisntence.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "planets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer swapiId;

    private String name;

    private String rotationPeriod;

    private String orbitalPeriod;

    private String diameter;

    private String climate;

    private String gravity;

    private String terrain;

    private String surfaceWater;

    private String population;

    private OffsetDateTime created;

    private OffsetDateTime edited;

    private String url;

    @OneToMany(mappedBy = "homeworld")
    @JsonManagedReference
    private Set<Character> residents = new HashSet<>();

    @ManyToMany(mappedBy = "planets")
    @JsonBackReference
    private Set<Film> films = new HashSet<>();
}
