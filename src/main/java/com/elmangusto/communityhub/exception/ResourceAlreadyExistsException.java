package com.elmangusto.communityhub.exception;

public class ResourceAlreadyExistsException extends ConflictException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
