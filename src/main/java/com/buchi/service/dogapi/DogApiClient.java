package com.buchi.service.dogapi;

import com.buchi.dto.response.PetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DogApiClient {

    private final WebClient webClient;

    public DogApiClient(@Value("${dogapi.base-url:https://api.thedogapi.com/v1}") String baseUrl,
                        @Value("${dogapi.api-key:}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    /**
     * Search The Dog API for dog images.
     * Only returns results when type filter is null or includes "Dog".
     * Gracefully returns empty list on any error.
     */
    public List<PetResponse> searchDogs(String typeFilter, int limit) {
        // Dog API only has dogs — skip if caller explicitly filtered for non-dogs
        if (typeFilter != null && !typeFilter.equalsIgnoreCase("Dog")) {
            return Collections.emptyList();
        }

        try {
            List<DogApiResponse> dogs = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/images/search")
                            .queryParam("limit", limit)
                            .queryParam("has_breeds", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<DogApiResponse>>() {})
                    .block();

            if (dogs == null || dogs.isEmpty()) {
                return Collections.emptyList();
            }

            List<PetResponse> results = dogs.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            log.info("Dog API returned {} dogs", results.size());
            return results;

        } catch (Exception e) {
            log.warn("Dog API unavailable, skipping: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private PetResponse mapToResponse(DogApiResponse dog) {
        String name = null;
        String description = null;

        if (dog.getBreeds() != null && !dog.getBreeds().isEmpty()) {
            DogApiResponse.Breed breed = dog.getBreeds().get(0);
            name = breed.getName();
            description = breed.getTemperament();
        }

        return PetResponse.builder()
                .petId("dog-" + dog.getId())
                .source("dogapi")
                .type("Dog")
                .gender("unknown")
                .size("medium")     // Dog API doesn't provide size directly
                .age("unknown")
                .name(name)
                .description(description)
                .goodWithChildren(null)
                .photos(dog.getUrl() != null ? List.of(dog.getUrl()) : List.of())
                .build();
    }
}