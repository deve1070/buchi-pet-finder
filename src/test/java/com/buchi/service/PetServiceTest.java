package com.buchi.service;

import com.buchi.dto.request.CreatePetRequest;
import com.buchi.dto.request.GetPetsRequest;
import com.buchi.dto.response.PetResponse;
import com.buchi.entity.Pet;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.repository.PetRepository;
import com.buchi.service.dogapi.DogApiClient;
import com.buchi.util.PhotoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService Tests")
class PetServiceTest {

    @Mock private PetRepository petRepository;
    @Mock private DogApiClient dogApiClient;
    @Mock private PhotoStorageService photoStorageService;

    @InjectMocks private PetService petService;

    private Pet samplePet;

    @BeforeEach
    void setUp() {
        samplePet = Pet.builder()
                .id(1L)
                .type(Pet.PetType.Dog)
                .gender(Pet.PetGender.male)
                .size(Pet.PetSize.small)
                .age(Pet.PetAge.baby)
                .goodWithChildren(true)
                .status("available")
                .build();
    }

    // ── createPet ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPet saves pet and returns response with correct pet_id")
    void createPet_savesAndReturnsId() {
        CreatePetRequest req = new CreatePetRequest();
        req.setType(Pet.PetType.Dog);
        req.setGender(Pet.PetGender.male);
        req.setSize(Pet.PetSize.small);
        req.setAge(Pet.PetAge.baby);
        req.setGoodWithChildren(true);

        when(petRepository.save(any(Pet.class))).thenReturn(samplePet);

        PetResponse result = petService.createPet(req, null);

        assertThat(result.getPetId()).isEqualTo("1");
        assertThat(result.getSource()).isEqualTo("local");
        assertThat(result.getType()).isEqualTo("Dog");
        verify(petRepository, atLeastOnce()).save(any(Pet.class));
    }

    @Test
    @DisplayName("createPet with no photos still saves successfully")
    void createPet_withoutPhotos_savesSuccessfully() {
        when(petRepository.save(any(Pet.class))).thenReturn(samplePet);

        CreatePetRequest req = new CreatePetRequest();
        req.setType(Pet.PetType.Cat);
        req.setGender(Pet.PetGender.female);
        req.setSize(Pet.PetSize.medium);
        req.setAge(Pet.PetAge.adult);
        req.setGoodWithChildren(false);

        PetResponse result = petService.createPet(req, null);

        assertThat(result).isNotNull();
        verifyNoInteractions(photoStorageService);
    }

    // ── searchPets ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchPets returns local results first")
    @SuppressWarnings("unchecked")
    void searchPets_localResultsFirst() {
        GetPetsRequest req = new GetPetsRequest();
        req.setLimit(3);

        when(petRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePet)));

        PetResponse dog1 = PetResponse.builder().petId("dog-101").source("dogapi").build();
        PetResponse dog2 = PetResponse.builder().petId("dog-102").source("dogapi").build();
        when(dogApiClient.searchDogs(any(), anyInt()))
                .thenReturn(List.of(dog1, dog2));

        List<PetResponse> results = petService.searchPets(req);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getSource()).isEqualTo("local");
        assertThat(results.get(1).getSource()).isEqualTo("dogapi");
        assertThat(results.get(2).getSource()).isEqualTo("dogapi");
    }

    @Test
    @DisplayName("searchPets does not call Petfinder when local fills the limit")
    @SuppressWarnings("unchecked")
    void searchPets_noPetfinderCallWhenLocalFillsLimit() {
        GetPetsRequest req = new GetPetsRequest();
        req.setLimit(1);

        when(petRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePet)));

        List<PetResponse> results = petService.searchPets(req);

        assertThat(results).hasSize(1);
        verifyNoInteractions(dogApiClient);
    }

    @Test
    @DisplayName("searchPets trims results to exact limit")
    @SuppressWarnings("unchecked")
    void searchPets_trimsToLimit() {
        GetPetsRequest req = new GetPetsRequest();
        req.setLimit(1);

        Pet pet2 = Pet.builder().id(2L).type(Pet.PetType.Cat)
                .gender(Pet.PetGender.female).size(Pet.PetSize.medium)
                .age(Pet.PetAge.adult).goodWithChildren(false)
                .status("available").build();

        when(petRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePet, pet2)));

        List<PetResponse> results = petService.searchPets(req);

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("searchPets returns empty list when nothing found anywhere")
    @SuppressWarnings("unchecked")
    void searchPets_returnsEmpty_whenNothingFound() {
        GetPetsRequest req = new GetPetsRequest();
        req.setLimit(5);

        when(petRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(dogApiClient.searchDogs(any(), anyInt())).thenReturn(List.of());

        List<PetResponse> results = petService.searchPets(req);

        assertThat(results).isEmpty();
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById returns pet when found")
    void findById_returnsPet() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(samplePet));

        Pet result = petService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(Pet.PetType.Dog);
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException for unknown id")
    void findById_throwsWhenNotFound() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}