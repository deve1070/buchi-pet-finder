package com.buchi.service.petfinder;

import com.buchi.dto.response.PetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetfinderClient {

    private final WebClient petfinderWebClient;

    @Value("${petfinder.api.key}")
    private String apiKey;

    @Value("${petfinder.api.secret}")
    private String apiSecret;

    // ── Token cache ────────────────────────────────────────────────────────────
    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private volatile Instant tokenExpiry = Instant.MIN;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Search Petfinder for animals matching the given filters.
     * Returns an empty list (never throws) if the API is unavailable or
     * credentials are not configured — so the app degrades gracefully.
     *
     * @param type              single pet type string e.g. "Dog" (nullable)
     * @param gender            e.g. "male" (nullable)
     * @param size              e.g. "small" (nullable)
     * @param age               e.g. "baby" (nullable)
     * @param goodWithChildren  filter by child-friendly (nullable)
     * @param limit             max results to fetch
     */
    public List<PetResponse> searchAnimals(
            String type,
            String gender,
            String size,
            String age,
            Boolean goodWithChildren,
            int limit) {
        try {
            String token = getAccessToken();

            PetfinderAnimalsResponse response = petfinderWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/animals");
                        if (type   != null) uriBuilder.queryParam("type",   type);
                        if (gender != null) uriBuilder.queryParam("gender", gender.toLowerCase());
                        if (size   != null) uriBuilder.queryParam("size",   size.toLowerCase());
                        if (age    != null) uriBuilder.queryParam("age",    age.toLowerCase());
                        uriBuilder.queryParam("limit", limit);
                        return uriBuilder.build();
                    })
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(PetfinderAnimalsResponse.class)
                    .block();

            if (response == null || response.getAnimals() == null) {
                return Collections.emptyList();
            }

            List<PetResponse> results = response.getAnimals().stream()
                    .map(animal -> mapToResponse(animal, goodWithChildren))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            log.info("Petfinder returned {} animals", results.size());
            return results;

        } catch (Exception e) {
            log.warn("Petfinder API unavailable, skipping: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Token management ───────────────────────────────────────────────────────

    private String getAccessToken() {
        if (cachedToken.get() != null && Instant.now().isBefore(tokenExpiry)) {
            log.debug("Using cached Petfinder token");
            return cachedToken.get();
        }

        log.info("Fetching new Petfinder access token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", apiKey);
        formData.add("client_secret", apiSecret);

        PetfinderTokenResponse tokenResponse = WebClient.create("https://api.petfinder.com/v2")
                .post()
                .uri("/oauth2/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(PetfinderTokenResponse.class)
                .block();

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to obtain Petfinder access token");
        }

        cachedToken.set(tokenResponse.getAccessToken());
        // Refresh 60s before actual expiry to avoid edge-case rejections
        tokenExpiry = Instant.now().plusSeconds(tokenResponse.getExpiresIn() - 60);

        return tokenResponse.getAccessToken();
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private PetResponse mapToResponse(
            PetfinderAnimalsResponse.PetfinderAnimal animal,
            Boolean requestedGoodWithChildren) {

        // If caller filtered by good_with_children, apply it here
        // (Petfinder doesn't support this as a query param)
        if (requestedGoodWithChildren != null) {
            Boolean animalGoodWithChildren = (animal.getEnvironment() != null)
                    ? animal.getEnvironment().getChildren()
                    : null;
            // Skip if the filter doesn't match
            if (animalGoodWithChildren != null
                    && !animalGoodWithChildren.equals(requestedGoodWithChildren)) {
                return null;
            }
        }

        List<String> photoUrls = (animal.getPhotos() != null)
                ? animal.getPhotos().stream()
                        .map(p -> p.getMedium() != null ? p.getMedium() : p.getSmall())
                        .filter(url -> url != null)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        Boolean goodWithChildren = (animal.getEnvironment() != null)
                ? animal.getEnvironment().getChildren()
                : null;

        return PetResponse.builder()
                .petId("pf-" + animal.getId())
                .source("petfinder")
                .type(animal.getType())
                .gender(animal.getGender())
                .size(animal.getSize())
                .age(animal.getAge())
                .name(animal.getName())
                .description(animal.getDescription())
                .status(animal.getStatus())
                .goodWithChildren(goodWithChildren)
                .photos(photoUrls)
                .build();
    }
}