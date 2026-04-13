package com.buchi.util;

import com.buchi.dto.request.GetPetsRequest;
import com.buchi.entity.Pet;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PetSpecification {

    public static Specification<Pet> fromRequest(GetPetsRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getType() != null && !request.getType().isEmpty()) {
                predicates.add(root.get("type").in(request.getType()));
            }
            if (request.getGender() != null && !request.getGender().isEmpty()) {
                predicates.add(root.get("gender").in(request.getGender()));
            }
            if (request.getSize() != null && !request.getSize().isEmpty()) {
                predicates.add(root.get("size").in(request.getSize()));
            }
            if (request.getAge() != null && !request.getAge().isEmpty()) {
                predicates.add(root.get("age").in(request.getAge()));
            }
            if (request.getGoodWithChildren() != null) {
                predicates.add(cb.equal(root.get("goodWithChildren"), request.getGoodWithChildren()));
            }

            predicates.add(cb.equal(root.get("status"), "available"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
