package com.buchi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimilarPetResponse {
    private PetResponse pet;
    private Integer similarityScore;
}

