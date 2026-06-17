package com.umbook.backend.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Las credenciales ingresadas no son válidas.");
    }
}
