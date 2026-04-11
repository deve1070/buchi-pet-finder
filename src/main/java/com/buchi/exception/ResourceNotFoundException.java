package com.buchi.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException pet(Long id) {
        return new ResourceNotFoundException("Pet not found with id: " + id);
    }

    public static ResourceNotFoundException customer(Long id) {
        return new ResourceNotFoundException("Customer not found with id: " + id);
    }
}