package ua.markiyan.sonara.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {
    private final String field;

    public ResourceAlreadyExistsException(String field, String message) {
        super(message);
        this.field = field;
    }
}