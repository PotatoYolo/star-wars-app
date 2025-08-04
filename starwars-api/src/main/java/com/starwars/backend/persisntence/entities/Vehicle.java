package com.starwars.backend.persisntence.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer swapiId;

    private String name;

    private String model;

    private String manufacturer;

    private String costInCredits;

    String vehicleClass;

    private String length;

    private String maxAtmospheringSpeed;

    private String crew;

    private String passengers;

    private String cargoCapacity;

    private String consumables;

    private OffsetDateTime created;

    private OffsetDateTime edited;

    private String url;

    @ManyToMany
    @JoinTable(
            name = "character_vehicle",
            joinColumns = @JoinColumn(name = "vehicle_id"),
            inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    @JsonManagedReference
    private Set<Character> pilots = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "vehicle_films",
            joinColumns = @JoinColumn(name = "vehicle_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    @JsonManagedReference
    private Set<Film> films = new HashSet<>();
}
