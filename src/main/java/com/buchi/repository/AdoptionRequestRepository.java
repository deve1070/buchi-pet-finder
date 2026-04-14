package com.buchi.repository;

import com.buchi.entity.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    // Used to prevent duplicate adoption requests
    boolean existsByCustomerIdAndPetId(Long customerId, Long petId);
}