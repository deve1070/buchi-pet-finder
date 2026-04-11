package com.buchi.dto.request;

import com.buchi.entity.Pet;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePetRequest {

    @NotNull(message = "type is required")
    private Pet.PetType type;

    @NotNull(message = "gender is required")
    private Pet.PetGender gender;

    @NotNull(message = "size is required")
    private Pet.PetSize size;

    @NotNull(message = "age is required")
    private Pet.PetAge age;

    @NotNull(message = "good_with_children is required")
    private Boolean goodWithChildren;

    private String name;
    private String description;
}