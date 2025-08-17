package com.cube.simple.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object key) {
        super(String.format("%s not found for id = %s", resourceName, key));
    }
}
