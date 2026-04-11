package com.buchi.dto.response;

import com.buchi.entity.Pet;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PetResponse {

    private String petId;
    private String source;  // "local" or "petfinder"
    private String type;
    private String gender;
    private String size;
    private String age;
    private Boolean goodWithChildren;
    private String name;
    private String description;
    private String status;
    private List<String> photos;

    public static PetResponse fromEntity(Pet pet) {
        return PetResponse.builder()
                .petId(String.valueOf(pet.getId()))
                .source("local")
                .type(pet.getType().name())
                .gender(pet.getGender().name())
                .size(pet.getSize().name())
                .age(pet.getAge().name())
                .goodWithChildren(pet.getGoodWithChildren())
                .name(pet.getName())
                .description(pet.getDescription())
                .status(pet.getStatus())
                .photos(pet.getPhotos().stream().map(p -> p.getUrl()).toList())
                .build();
    }
}