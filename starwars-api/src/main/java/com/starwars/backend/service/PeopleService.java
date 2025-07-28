package com.starwars.backend.service;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.PersonDTO;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class PeopleService {

    private final SwapiService swapiService;

    public PeopleService(SwapiService swapiService) {
        this.swapiService = swapiService;
    }

    public Page<PersonDTO> getPeople(Pageable pageable, String search) {
        List<PersonDTO> all = swapiService.getAllPeople();

        Comparator<PersonDTO> comparator = Utils.buildComparator(
                pageable.getSort(),
                Map.of(
                        "name", PersonDTO::name,
                        "created", PersonDTO::created
                ),
                "name"
        );

        List<PersonDTO> filtered = all.stream()
                .filter(p -> search == null || p.name().toLowerCase().contains(search.toLowerCase()))
                .sorted(comparator)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}
