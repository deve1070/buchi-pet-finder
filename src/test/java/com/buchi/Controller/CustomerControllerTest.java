package com.buchi.controller;

import com.buchi.entity.Customer;
import com.buchi.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CustomerService customerService;

    @Test
    @DisplayName("POST /add_customer returns 201 with customer_id")
    void addCustomer_returns201() throws Exception {
        Customer customer = Customer.builder()
                .id(1L).name("Abebe Kebede").phone("0912345678").build();

        when(customerService.addOrGetCustomer(any())).thenReturn(customer);

        mockMvc.perform(post("/api/v1/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Abebe Kebede", "phone", "0912345678"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customer_id").value("1"));
    }

    @Test
    @DisplayName("POST /add_customer returns 201 with same id for duplicate phone")
    void addCustomer_returnsSameId_forDuplicatePhone() throws Exception {
        Customer customer = Customer.builder()
                .id(1L).name("Abebe Kebede").phone("0912345678").build();

        when(customerService.addOrGetCustomer(any())).thenReturn(customer);

        mockMvc.perform(post("/api/v1/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Different Name", "phone", "0912345678"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customer_id").value("1"));
    }

    @Test
    @DisplayName("POST /add_customer returns 400 when phone is missing")
    void addCustomer_returns400_whenPhoneMissing() throws Exception {
        mockMvc.perform(post("/api/v1/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Abebe Kebede"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /add_customer returns 400 when name is missing")
    void addCustomer_returns400_whenNameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("phone", "0912345678"))))
                .andExpect(status().isBadRequest());
    }
}