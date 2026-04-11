package com.buchi.service;

import com.buchi.dto.request.CreatePetRequest;
import com.buchi.dto.response.PetResponse;
import com.buchi.entity.Pet;
import com.buchi.entity.PetPhoto;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.repository.PetRepository;
import com.buchi.util.PhotoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {

    private final PetRepository petRepository;
    private final PhotoStorageService photoStorageService;

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

    @Transactional(readOnly = true)
    public Pet findById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.pet(id));
    }
}