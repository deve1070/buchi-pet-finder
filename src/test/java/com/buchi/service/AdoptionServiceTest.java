package com.buchi.service;

import com.buchi.dto.request.AdoptRequest;
import com.buchi.dto.request.DateRangeRequest;
import com.buchi.dto.response.AdoptionRequestResponse;
import com.buchi.entity.AdoptionRequest;
import com.buchi.entity.Customer;
import com.buchi.entity.Pet;
import com.buchi.exception.BadRequestException;
import com.buchi.repository.AdoptionRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdoptionService Tests")
class AdoptionServiceTest {

    @Mock private AdoptionRequestRepository adoptionRepo;
    @Mock private CustomerService customerService;
    @Mock private PetService petService;

    @InjectMocks private AdoptionService adoptionService;

    private Customer customer;
    private Pet pet;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L).name("Abebe").phone("0911111111").build();

        pet = Pet.builder()
                .id(2L).type(Pet.PetType.Cat)
                .gender(Pet.PetGender.female)
                .size(Pet.PetSize.small)
                .age(Pet.PetAge.young)
                .goodWithChildren(true)
                .status("available")
                .build();
    }

    // ── createAdoption ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAdoption saves and returns adoption request")
    void createAdoption_success() {
        AdoptRequest req = new AdoptRequest();
        req.setCustomerId(1L);
        req.setPetId(2L);

        when(customerService.findById(1L)).thenReturn(customer);
        when(petService.findById(2L)).thenReturn(pet);
        when(adoptionRepo.existsByCustomerIdAndPetId(1L, 2L)).thenReturn(false);
        when(adoptionRepo.save(any(AdoptionRequest.class))).thenAnswer(inv -> {
            AdoptionRequest a = inv.getArgument(0);
            return AdoptionRequest.builder()
                    .id(10L)
                    .customer(a.getCustomer())
                    .pet(a.getPet())
                    .status("pending")
                    .build();
        });

        AdoptionRequest result = adoptionService.createAdoption(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("pending");
        verify(adoptionRepo).save(any(AdoptionRequest.class));
    }

    @Test
    @DisplayName("createAdoption throws BadRequestException on duplicate")
    void createAdoption_throwsOnDuplicate() {
        AdoptRequest req = new AdoptRequest();
        req.setCustomerId(1L);
        req.setPetId(2L);

        when(customerService.findById(1L)).thenReturn(customer);
        when(petService.findById(2L)).thenReturn(pet);
        when(adoptionRepo.existsByCustomerIdAndPetId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> adoptionService.createAdoption(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has an adoption request");

        verify(adoptionRepo, never()).save(any());
    }

    @Test
    @DisplayName("createAdoption sets status to pending")
    void createAdoption_statusIsPending() {
        AdoptRequest req = new AdoptRequest();
        req.setCustomerId(1L);
        req.setPetId(2L);

        when(customerService.findById(1L)).thenReturn(customer);
        when(petService.findById(2L)).thenReturn(pet);
        when(adoptionRepo.existsByCustomerIdAndPetId(1L, 2L)).thenReturn(false);
        when(adoptionRepo.save(any(AdoptionRequest.class))).thenAnswer(inv -> {
            AdoptionRequest a = inv.getArgument(0);
            return AdoptionRequest.builder()
                    .id(1L).customer(a.getCustomer()).pet(a.getPet())
                    .status(a.getStatus()).build();
        });

        AdoptionRequest result = adoptionService.createAdoption(req);

        assertThat(result.getStatus()).isEqualTo("pending");
    }

    // ── getAdoptionRequests ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAdoptionRequests returns mapped list for valid date range")
    void getAdoptionRequests_returnsMappedList() {
        AdoptionRequest adoption = AdoptionRequest.builder()
                .id(1L).customer(customer).pet(pet)
                .status("pending")
                .requestedAt(OffsetDateTime.now())
                .build();

        when(adoptionRepo.findByDateRange(any(), any())).thenReturn(List.of(adoption));

        DateRangeRequest range = new DateRangeRequest();
        range.setFromDate(LocalDate.of(2024, 1, 1));
        range.setToDate(LocalDate.of(2027, 12, 31));

        List<AdoptionRequestResponse> results = adoptionService.getAdoptionRequests(range);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCustomerId()).isEqualTo("1");
        assertThat(results.get(0).getPetId()).isEqualTo("2");
        assertThat(results.get(0).getType()).isEqualTo("Cat");
        assertThat(results.get(0).getStatus()).isEqualTo("pending");
    }

    @Test
    @DisplayName("getAdoptionRequests returns empty list when no data in range")
    void getAdoptionRequests_returnsEmpty_whenNoData() {
        when(adoptionRepo.findByDateRange(any(), any())).thenReturn(List.of());

        DateRangeRequest range = new DateRangeRequest();
        range.setFromDate(LocalDate.of(2020, 1, 1));
        range.setToDate(LocalDate.of(2020, 12, 31));

        List<AdoptionRequestResponse> results = adoptionService.getAdoptionRequests(range);

        assertThat(results).isEmpty();
    }
}