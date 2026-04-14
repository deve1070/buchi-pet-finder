package com.buchi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptRequest {

    @NotNull(message = "customer_id is required")
    private Long customerId;

    @NotNull(message = "pet_id is required")
    private Long petId;

    private String notes;
}