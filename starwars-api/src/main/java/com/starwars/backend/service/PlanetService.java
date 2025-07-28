package com.starwars.backend.service;

import com.starwars.backend.Utils;
import com.starwars.backend.dto.PlanetDTO;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class PlanetService {

    private final SwapiService swapiService;

    public PlanetService(SwapiService swapiService) {
        this.swapiService = swapiService;
    }

    public Page<PlanetDTO> getPlanets(Pageable pageable, String search) {
        List<PlanetDTO> all = swapiService.getAllPlanets();

        Comparator<PlanetDTO> comparator = Utils.buildComparator(
                pageable.getSort(),
                Map.of(
                        "name", PlanetDTO::name,
                        "created", PlanetDTO::created
                ),
                "name"
        );

        List<PlanetDTO> filtered = all.stream()
                .filter(p -> search == null || p.name().toLowerCase().contains(search.toLowerCase()))
                .sorted(comparator)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}
