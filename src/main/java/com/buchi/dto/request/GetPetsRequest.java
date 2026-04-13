package com.buchi.dto.request;

import com.buchi.entity.Pet;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetPetsRequest {

    // All filters are optional except limit
    private List<Pet.PetType> type;
    private List<Pet.PetGender> gender;
    private List<Pet.PetSize> size;
    private List<Pet.PetAge> age;
    private Boolean goodWithChildren;

    @NotNull(message = "limit is required")
    @Min(value = 1, message = "limit must be at least 1")
    private Integer limit;
}