package com.buchi.Controller;

import com.buchi.entity.AdoptionRequest;
import com.buchi.controller.AdoptionController;
import com.buchi.entity.Customer;
import com.buchi.entity.Pet;
import com.buchi.exception.BadRequestException;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.service.AdoptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdoptionController.class)
@DisplayName("AdoptionController Tests")
class AdoptionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AdoptionService adoptionService;

    @Test
    @DisplayName("POST /adopt returns 201 with adoption_id")
    void adopt_returns201() throws Exception {
        Customer customer = Customer.builder().id(1L).name("Abebe").phone("09111").build();
        Pet pet = Pet.builder().id(2L).type(Pet.PetType.Dog)
                .gender(Pet.PetGender.male).size(Pet.PetSize.small)
                .age(Pet.PetAge.baby).goodWithChildren(true)
                .status("available").build();

        AdoptionRequest adoption = AdoptionRequest.builder()
                .id(10L).customer(customer).pet(pet).status("pending").build();

        when(adoptionService.createAdoption(any())).thenReturn(adoption);

        mockMvc.perform(post("/api/v1/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("customerId", 1, "petId", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.adoption_id").value("10"));
    }

    @Test
    @DisplayName("POST /adopt returns 404 when customer not found")
    void adopt_returns404_whenCustomerNotFound() throws Exception {
        when(adoptionService.createAdoption(any()))
                .thenThrow(ResourceNotFoundException.customer(999L));

        mockMvc.perform(post("/api/v1/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("customerId", 999, "petId", 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /adopt returns 404 when pet not found")
    void adopt_returns404_whenPetNotFound() throws Exception {
        when(adoptionService.createAdoption(any()))
                .thenThrow(ResourceNotFoundException.pet(999L));

        mockMvc.perform(post("/api/v1/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("customerId", 1, "petId", 999))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /adopt returns 400 on duplicate adoption")
    void adopt_returns400_onDuplicate() throws Exception {
        when(adoptionService.createAdoption(any()))
                .thenThrow(new BadRequestException("already has an adoption request"));

        mockMvc.perform(post("/api/v1/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("customerId", 1, "petId", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /adopt returns 400 when customerId is missing")
    void adopt_returns400_whenCustomerIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("petId", 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /adoption_stats returns 200 with stats payload")
    void adoptionStats_returns200() throws Exception {
        var stats = com.buchi.dto.response.AdoptionStatsResponse.builder()
                .totalAdoptionRequests(10L)
                .uniqueCustomers(7L)
                .byStatus(Map.of("pending", 8L, "approved", 2L))
                .topPets(List.of(Map.of("pet_id", "2", "adoption_requests", 4L)))
                .build();

        when(adoptionService.getAdoptionStats(isNull(), isNull(), anyInt()))
                .thenReturn(stats);

        mockMvc.perform(get("/api/v1/adoption_stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.data.totalAdoptionRequests").value(10))
                .andExpect(jsonPath("$.data.data.uniqueCustomers").value(7))
                .andExpect(jsonPath("$.data.data.byStatus.pending").value(8));
    }
}