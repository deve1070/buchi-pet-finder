package com.buchi.service;

import com.buchi.dto.request.AdoptRequest;
import com.buchi.entity.AdoptionRequest;
import com.buchi.entity.Customer;
import com.buchi.entity.Pet;
import com.buchi.exception.BadRequestException;
import com.buchi.repository.AdoptionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionService {

    private final AdoptionRequestRepository adoptionRepo;
    private final CustomerService customerService;
    private final PetService petService;

    @Transactional
    public AdoptionRequest createAdoption(AdoptRequest request) {
        // 1. Validate both IDs exist — throws 404 if not
        Customer customer = customerService.findById(request.getCustomerId());
        Pet pet = petService.findById(request.getPetId());

        // 2. Prevent duplicate adoption request
        if (adoptionRepo.existsByCustomerIdAndPetId(customer.getId(), pet.getId())) {
            throw new BadRequestException(
                "Customer " + customer.getId() +
                " already has an adoption request for pet " + pet.getId()
            );
        }

        // 3. Create the adoption request
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
}