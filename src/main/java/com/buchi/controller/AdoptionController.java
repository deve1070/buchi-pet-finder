package com.buchi.controller;

import com.buchi.dto.request.AdoptRequest;
import com.buchi.dto.request.DateRangeRequest;
import com.buchi.dto.response.AdoptionRequestResponse;
import com.buchi.dto.response.AdoptionStatsResponse;
import com.buchi.dto.response.ApiResponse;
import com.buchi.dto.response.ReportResponse;
import com.buchi.entity.AdoptionRequest;
import com.buchi.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Adoptions", description = "Adoption management endpoints")
public class AdoptionController {

    private final AdoptionService adoptionService;

    // ── POST /adopt ────────────────────────────────────────────────────────────

    @Operation(
        summary = "Request adoption",
        description = "Creates an adoption request. " +
                      "Returns 404 if customer_id or pet_id does not exist. " +
                      "Returns 400 if the pair already has a pending request."
    )
    @PostMapping("/adopt")
    public ResponseEntity<ApiResponse<Map<String, String>>> adopt(
            @RequestBody @Valid AdoptRequest request) {

        AdoptionRequest adoption = adoptionService.createAdoption(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("adoption_id", String.valueOf(adoption.getId()))));
    }

    // ── GET /get_adoption_requests ─────────────────────────────────────────────

    @Operation(
        summary = "Get adoption requests in a date range",
        description = "Returns all adoption requests between from_date and to_date. " +
                      "Ordered oldest first. Date format: yyyy-MM-dd"
    )
    @GetMapping("/get_adoption_requests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdoptionRequests(
            @RequestParam("from_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        DateRangeRequest request = new DateRangeRequest();
        request.setFromDate(fromDate);
        request.setToDate(toDate);

        List<AdoptionRequestResponse> data = adoptionService.getAdoptionRequests(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("data", data)));
    }

    // ── POST /generate_report ──────────────────────────────────────────────────

    @Operation(
        summary = "Generate adoption report (Bonus)",
        description = "Returns adoption statistics for a date range: " +
                      "pet type breakdown, weekly counts, total adoptions, unique customers."
    )
    @PostMapping("/generate_report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateReport(
            @RequestBody @Valid DateRangeRequest request) {

        ReportResponse report = adoptionService.generateReport(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("data", report)));
    }

    // ── GET /adoption_stats ────────────────────────────────────────────────────

    @Operation(
        summary = "Get adoption stats",
        description = "Returns aggregate adoption stats, optionally filtered by from_date/to_date (yyyy-MM-dd)."
    )
    @GetMapping("/adoption_stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adoptionStats(
            @RequestParam(value = "from_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "to_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "top_pets_limit", defaultValue = "5") Integer topPetsLimit) {

        AdoptionStatsResponse stats = adoptionService.getAdoptionStats(fromDate, toDate, topPetsLimit);
        return ResponseEntity.ok(ApiResponse.success(Map.of("data", stats)));
    }
}