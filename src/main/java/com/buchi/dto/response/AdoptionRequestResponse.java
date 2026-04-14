package com.buchi.dto.response;

import com.buchi.entity.AdoptionRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdoptionRequestResponse {

    private String customerId;
    private String customerPhone;
    private String customerName;
    private String petId;
    private String type;
    private String gender;
    private String size;
    private String age;
    private Boolean goodWithChildren;
    private String status;
    private String requestedAt;

    public static AdoptionRequestResponse fromEntity(AdoptionRequest a) {
        return AdoptionRequestResponse.builder()
                .customerId(String.valueOf(a.getCustomer().getId()))
                .customerPhone(a.getCustomer().getPhone())
                .customerName(a.getCustomer().getName())
                .petId(String.valueOf(a.getPet().getId()))
                .type(a.getPet().getType().name())
                .gender(a.getPet().getGender().name())
                .size(a.getPet().getSize().name())
                .age(a.getPet().getAge().name())
                .goodWithChildren(a.getPet().getGoodWithChildren())
                .status(a.getStatus())
                .requestedAt(a.getRequestedAt().toString())
                .build();
    }
}
