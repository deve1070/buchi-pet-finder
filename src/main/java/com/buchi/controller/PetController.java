package com.buchi.controller;

import com.buchi.dto.request.CreatePetRequest;
import com.buchi.dto.request.GetPetsRequest;
import com.buchi.dto.response.ApiResponse;
import com.buchi.dto.response.PetResponse;
import com.buchi.entity.Pet;
import com.buchi.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Pet management endpoints")
public class PetController {

    private final PetService petService;

    // ── POST /create_pet ───────────────────────────────────────────────────────

    @Operation(
        summary = "Create a new pet",
        description = "Creates a pet in the local database with optional photos."
    )
    @PostMapping(value = "/create_pet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> createPet(
            @RequestParam("type") Pet.PetType type,
            @RequestParam("gender") Pet.PetGender gender,
            @RequestParam("size") Pet.PetSize size,
            @RequestParam("age") Pet.PetAge age,
            @RequestParam("good_with_children") Boolean goodWithChildren,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        CreatePetRequest request = new CreatePetRequest();
        request.setType(type);
        request.setGender(gender);
        request.setSize(size);
        request.setAge(age);
        request.setGoodWithChildren(goodWithChildren);
        request.setName(name);
        request.setDescription(description);

        PetResponse pet = petService.createPet(request, photos);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("pet_id", pet.getPetId())));
    }

    // ── GET /get_pets ──────────────────────────────────────────────────────────

    @Operation(
        summary = "Search for pets",
        description = "Searches local DB first, then fills remaining slots from Petfinder. " +
                      "All filters are optional except limit. " +
                      "type, gender, size, age support multiple values."
    )
    @GetMapping("/get_pets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPets(
            @RequestParam(required = false) List<Pet.PetType> type,
            @RequestParam(required = false) List<Pet.PetGender> gender,
            @RequestParam(required = false) List<Pet.PetSize> size,
            @RequestParam(required = false) List<Pet.PetAge> age,
            @RequestParam(name = "good_with_children", required = false) Boolean goodWithChildren,
            @RequestParam Integer limit) {

        GetPetsRequest request = new GetPetsRequest();
        request.setType(type);
        request.setGender(gender);
        request.setSize(size);
        request.setAge(age);
        request.setGoodWithChildren(goodWithChildren);
        request.setLimit(limit);

        List<PetResponse> pets = petService.searchPets(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("pets", pets)));
    }
}