package com.buchi.Controller;

import com.buchi.controller.PetController;
import com.buchi.dto.response.PetResponse;
import com.buchi.service.PetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@DisplayName("PetController Tests")
class PetControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private PetService petService;

    @Test
    @DisplayName("GET /get_pets returns 200 with pets array")
    void getPets_returns200() throws Exception {
        PetResponse pet = PetResponse.builder()
                .petId("1").source("local").type("Dog")
                .gender("male").size("small").age("baby")
                .goodWithChildren(true).photos(List.of())
                .build();

        when(petService.searchPets(any())).thenReturn(List.of(pet));

        mockMvc.perform(get("/api/v1/get_pets").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.pets").isArray())
                .andExpect(jsonPath("$.data.pets[0].source").value("local"));
    }

    @Test
    @DisplayName("GET /get_pets returns 200 with empty list when no pets")
    void getPets_returns200_withEmptyList() throws Exception {
        when(petService.searchPets(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/get_pets").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pets").isArray())
                .andExpect(jsonPath("$.data.pets").isEmpty());
    }

    @Test
    @DisplayName("GET /get_pets returns 400 when limit is missing")
    void getPets_returns400_whenLimitMissing() throws Exception {
        mockMvc.perform(get("/api/v1/get_pets"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /get_pets filters by type")
    void getPets_filtersByType() throws Exception {
        PetResponse pet = PetResponse.builder()
                .petId("1").source("local").type("Cat")
                .gender("female").size("small").age("adult")
                .goodWithChildren(false).photos(List.of())
                .build();

        when(petService.searchPets(any())).thenReturn(List.of(pet));

        mockMvc.perform(get("/api/v1/get_pets")
                        .param("type", "Cat")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pets[0].type").value("Cat"));
    }
}