package com.buchi.controller;

import com.buchi.dto.request.AdoptRequest;
import com.buchi.dto.response.ApiResponse;
import com.buchi.entity.AdoptionRequest;
import com.buchi.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Adoptions", description = "Adoption management endpoints")
public class AdoptionController {

    private final AdoptionService adoptionService;

    @Operation(
        summary = "Request adoption",
        description = "Creates an adoption request for a customer and pet. " +
                      "Returns 404 if customer_id or pet_id does not exist. " +
                      "Returns 400 if adoption request already exists for this pair."
    )
    @PostMapping("/adopt")
    public ResponseEntity<ApiResponse<Map<String, String>>> adopt(
            @RequestBody @Valid AdoptRequest request) {

        AdoptionRequest adoption = adoptionService.createAdoption(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("adoption_id", String.valueOf(adoption.getId()))));
    }
}