package com.buchi.service;

import com.buchi.dto.request.AdoptRequest;
import com.buchi.dto.request.DateRangeRequest;
import com.buchi.dto.response.AdoptionRequestResponse;
import com.buchi.dto.response.ReportResponse;
import com.buchi.entity.AdoptionRequest;
import com.buchi.entity.Customer;
import com.buchi.entity.Pet;
import com.buchi.exception.BadRequestException;
import com.buchi.repository.AdoptionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionService {

    private final AdoptionRequestRepository adoptionRepo;
    private final CustomerService customerService;
    private final PetService petService;

    // ── Create adoption ────────────────────────────────────────────────────────

    @Transactional
    public AdoptionRequest createAdoption(AdoptRequest request) {
        Customer customer = customerService.findById(request.getCustomerId());
        Pet pet = petService.findById(request.getPetId());

        if (adoptionRepo.existsByCustomerIdAndPetId(customer.getId(), pet.getId())) {
            throw new BadRequestException(
                "Customer " + customer.getId() +
                " already has an adoption request for pet " + pet.getId()
            );
        }

        AdoptionRequest adoption = AdoptionRequest.builder()
                .customer(customer)
                .pet(pet)
                .notes(request.getNotes())
                .status("pending")
                .build();

        AdoptionRequest saved = adoptionRepo.save(adoption);
        log.info("Adoption request created id={} customer={} pet={}",
                saved.getId(), customer.getId(), pet.getId());
        return saved;
    }

    // ── Get adoption requests by date range ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdoptionRequestResponse> getAdoptionRequests(DateRangeRequest request) {
        OffsetDateTime from = toStartOfDay(request.getFromDate());
        OffsetDateTime to   = toEndOfDay(request.getToDate());

        log.info("Fetching adoption requests from {} to {}", from, to);

        return adoptionRepo.findByDateRange(from, to)
                .stream()
                .map(AdoptionRequestResponse::fromEntity)
                .toList();
    }

    // ── Generate report ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReportResponse generateReport(DateRangeRequest request) {
        OffsetDateTime from = toStartOfDay(request.getFromDate());
        OffsetDateTime to   = toEndOfDay(request.getToDate());

        log.info("Generating report from {} to {}", from, to);

        // 1. Pet type breakdown
        Map<String, Long> adoptedPetTypes = new LinkedHashMap<>();
        for (Object[] row : adoptionRepo.countByPetTypeInRange(from, to)) {
            String typeName = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            adoptedPetTypes.put(typeName, count);
        }

        // 2. Weekly adoption counts
        Map<String, Long> weeklyAdoptions = new LinkedHashMap<>();
        for (Object[] row : adoptionRepo.countByWeekInRange(from, to)) {
            String weekStart = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            weeklyAdoptions.put(weekStart, count);
        }

        // 3. Total adoptions and unique customers in range
        List<AdoptionRequest> allInRange = adoptionRepo.findByDateRange(from, to);
        long totalAdoptions = allInRange.size();
        long uniqueCustomers = allInRange.stream()
                .map(a -> a.getCustomer().getId())
                .distinct()
                .count();

        return ReportResponse.builder()
                .adoptedPetTypes(adoptedPetTypes)
                .weeklyAdoptionRequests(weeklyAdoptions)
                .totalAdoptions(totalAdoptions)
                .uniqueCustomers(uniqueCustomers)
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private OffsetDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime toEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
    }
}