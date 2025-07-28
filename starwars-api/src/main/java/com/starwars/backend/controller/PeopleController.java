package com.starwars.backend.controller;

import com.starwars.backend.dto.PersonDTO;
import com.starwars.backend.service.PeopleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final PeopleService service;

    public PeopleController(PeopleService service) {
        this.service = service;
    }


    @GetMapping
    public Page<PersonDTO> getPeople(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        System.out.println("🔎 search = " + search);
        System.out.println("↕ sort = " + pageable.getSort());
        return service.getPeople(pageable, search);
    }
}
