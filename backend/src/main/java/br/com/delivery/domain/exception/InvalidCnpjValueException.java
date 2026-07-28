package br.com.delivery.domain.exception;

public class InvalidCnpjValueException extends RuntimeException {
    public InvalidCnpjValueException(String message) {
        super(message);
    }
}
