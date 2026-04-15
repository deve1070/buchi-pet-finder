package com.buchi.service;

import com.buchi.dto.request.AddCustomerRequest;
import com.buchi.entity.Customer;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CustomerService customerService;

    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        existingCustomer = Customer.builder()
                .id(1L)
                .name("Abebe Kebede")
                .phone("0912345678")
                .build();
    }

    // ── addOrGetCustomer ───────────────────────────────────────────────────────

    @Test
    @DisplayName("addOrGetCustomer creates new customer when phone not found")
    void addOrGetCustomer_createsNew_whenPhoneNotFound() {
        AddCustomerRequest req = new AddCustomerRequest();
        req.setName("New User");
        req.setPhone("0911111111");

        when(customerRepository.findByPhone("0911111111")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return Customer.builder()
                    .id(2L)
                    .name(c.getName())
                    .phone(c.getPhone())
                    .build();
        });

        Customer result = customerService.addOrGetCustomer(req);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("New User");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("addOrGetCustomer returns existing customer when phone already exists")
    void addOrGetCustomer_returnsExisting_whenPhoneExists() {
        AddCustomerRequest req = new AddCustomerRequest();
        req.setName("Different Name");
        req.setPhone("0912345678");

        when(customerRepository.findByPhone("0912345678"))
                .thenReturn(Optional.of(existingCustomer));

        Customer result = customerService.addOrGetCustomer(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Abebe Kebede");
        // Crucially: no save call should happen
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("addOrGetCustomer returns same id on duplicate — no new row")
    void addOrGetCustomer_sameIdOnDuplicate() {
        AddCustomerRequest req1 = new AddCustomerRequest();
        req1.setName("Abebe");
        req1.setPhone("0912345678");

        AddCustomerRequest req2 = new AddCustomerRequest();
        req2.setName("Someone Else");
        req2.setPhone("0912345678");

        when(customerRepository.findByPhone("0912345678"))
                .thenReturn(Optional.of(existingCustomer));

        Customer first  = customerService.addOrGetCustomer(req1);
        Customer second = customerService.addOrGetCustomer(req2);

        assertThat(first.getId()).isEqualTo(second.getId());
        verify(customerRepository, never()).save(any());
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById returns customer when found")
    void findById_returnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));

        Customer result = customerService.findById(1L);

        assertThat(result.getPhone()).isEqualTo("0912345678");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException for unknown id")
    void findById_throwsWhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}