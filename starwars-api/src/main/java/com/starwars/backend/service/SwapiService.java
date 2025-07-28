package com.starwars.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.backend.Utils;
import com.starwars.backend.dto.PersonDTO;
import com.starwars.backend.dto.PlanetDTO;
import com.starwars.backend.dto.SwapiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SwapiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SwapiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    public List<PersonDTO> getAllPeople() {
        String url = Utils.URL_PEOPLE;
        List<PersonDTO> allPeople = new ArrayList<>();

        while (url != null) {
            try {
                String json = restTemplate.getForObject(url, String.class);

                JsonNode node = objectMapper.readTree(json);
                JsonNode resultsNode = node.get("results");

                for (JsonNode personNode : resultsNode) {
                    PersonDTO person = new PersonDTO(
                            personNode.get("name").asText(),
                            personNode.get("height").asText(),
                            personNode.get("mass").asText(),
                            OffsetDateTime.parse(personNode.get("created").asText()),
                            personNode.get("url").asText()
                    );
                    allPeople.add(person);
                }

                JsonNode nextNode = node.get("next");
                url = (nextNode != null && !nextNode.isNull()) ? nextNode.asText() : null;

            } catch (Exception e) {
                throw new RuntimeException("Error retrieving people from SWAPI", e);
            }
        }

        return allPeople;
    }

    public List<PlanetDTO> getAllPlanets() {
        String url = Utils.URL_PLANETS;
        List<PlanetDTO> allPlanets = new ArrayList<>();

        while (url != null) {
            try {
                String json = restTemplate.getForObject(url, String.class);
                JsonNode node = objectMapper.readTree(json);
                JsonNode resultsNode = node.get("results");

                for (JsonNode planetNode : resultsNode) {
                    PlanetDTO planet = new PlanetDTO(
                            planetNode.get("name").asText(),
                            planetNode.get("climate").asText(),
                            planetNode.get("population").asText(),
                            OffsetDateTime.parse(planetNode.get("created").asText()),
                            planetNode.get("url").asText()
                    );
                    allPlanets.add(planet);
                }

                JsonNode nextNode = node.get("next");
                url = (nextNode != null && !nextNode.isNull()) ? nextNode.asText() : null;

            } catch (Exception e) {
                throw new RuntimeException("Error retrieving planets from SWAPI", e);
            }
        }

        return allPlanets;
    }

}
