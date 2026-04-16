package com.buchi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdoptionStatsResponse {
    private Long totalAdoptionRequests;
    private Long uniqueCustomers;
    private Map<String, Long> byStatus;
    private List<Map<String, Object>> topPets;
}

