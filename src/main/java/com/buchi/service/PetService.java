package com.buchi.service;

import com.buchi.dto.request.CreatePetRequest;
import com.buchi.dto.request.GetPetsRequest;
import com.buchi.dto.response.PetResponse;
import com.buchi.dto.response.SimilarPetResponse;
import com.buchi.entity.Pet;
import com.buchi.entity.PetPhoto;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.repository.PetRepository;
import com.buchi.service.dogapi.DogApiClient;
import com.buchi.util.PetSpecification;
import com.buchi.util.PhotoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {

    private final PetRepository petRepository;
    private final DogApiClient dogApiClient;
    private final PhotoStorageService photoStorageService;

    // ── Create ─────────────────────────────────────────────────────────────────

    @Transactional
    public PetResponse createPet(CreatePetRequest request, List<MultipartFile> photos) {
        Pet pet = Pet.builder()
                .type(request.getType())
                .gender(request.getGender())
                .size(request.getSize())
                .age(request.getAge())
                .goodWithChildren(request.getGoodWithChildren())
                .name(request.getName())
                .description(request.getDescription())
                .status("available")
                .build();

        pet = petRepository.save(pet);

        if (photos != null && !photos.isEmpty()) {
            List<PetPhoto> petPhotos = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (file != null && !file.isEmpty()) {
                    try {
                        PhotoStorageService.StoredPhoto stored = photoStorageService.store(file);
                        PetPhoto photo = PetPhoto.builder()
                                .pet(pet)
                                .filePath(stored.filePath())
                                .url(stored.url())
                                .isPrimary(i == 0)
                                .build();
                        petPhotos.add(photo);
                    } catch (IOException e) {
                        log.error("Failed to store photo for pet {}: {}", pet.getId(), e.getMessage());
                    }
                }
            }
            pet.getPhotos().addAll(petPhotos);
            pet = petRepository.save(pet);
        }

        log.info("Created pet id={} type={}", pet.getId(), pet.getType());
        return PetResponse.fromEntity(pet);
    }

    // ── Search ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PetResponse> searchPets(GetPetsRequest request) {
        int limit = request.getLimit();
        String typeFilter = firstValue(request.getType());

        // 1. Local DB first (always top results per spec)
        var spec = PetSpecification.fromRequest(request);
        List<Pet> localPets = petRepository
                .findAll(spec, PageRequest.of(0, limit))
                .getContent();

        List<PetResponse> results = new ArrayList<>(
                localPets.stream().map(PetResponse::fromEntity).toList()
        );
        log.info("Local DB returned {} pets", results.size());

        // 2. Dog API fills remaining slots
        int remaining = limit - results.size();
        if (remaining > 0) {
            List<PetResponse> dogApiResults = dogApiClient.searchDogs(typeFilter, remaining);
            results.addAll(dogApiResults);
            log.info("Dog API added {} pets", dogApiResults.size());
        }

        // 3. Trim to exact limit
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    // ── Similarity search ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SimilarPetResponse> findSimilarPets(Long petId, int limit) {
        Pet base = findById(petId);

        // Keep candidate set bounded for performance.
        // Strategy: same type + available + not itself, then score in-memory.
        List<Pet> candidates = petRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("type"), base.getType()),
                cb.equal(root.get("status"), "available"),
                cb.notEqual(root.get("id"), petId)
        ), PageRequest.of(0, Math.max(1, Math.min(200, limit * 40)))).getContent();

        return candidates.stream()
                .map(p -> SimilarPetResponse.builder()
                        .pet(PetResponse.fromEntity(p))
                        .similarityScore(similarityScore(base, p))
                        .build())
                .sorted(Comparator
                        .comparing(SimilarPetResponse::getSimilarityScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(r -> Long.valueOf(r.getPet().getPetId())))
                .limit(Math.max(0, limit))
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Pet findById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.pet(id));
    }

    private <T extends Enum<T>> String firstValue(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(0).name();
    }

    private int similarityScore(Pet base, Pet other) {
        int score = 0;
        if (base.getGender() != null && base.getGender().equals(other.getGender())) score += 2;
        if (base.getSize() != null && base.getSize().equals(other.getSize())) score += 2;
        if (base.getAge() != null && base.getAge().equals(other.getAge())) score += 2;
        if (base.getGoodWithChildren() != null
                && base.getGoodWithChildren().equals(other.getGoodWithChildren())) score += 1;
        return score;
    }
}