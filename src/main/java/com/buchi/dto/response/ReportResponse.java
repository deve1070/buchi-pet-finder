package com.buchi.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ReportResponse {

    private Map<String, Long> adoptedPetTypes;

    private Map<String, Long> weeklyAdoptionRequests;

    private Long totalAdoptions;
    private Long uniqueCustomers;
}