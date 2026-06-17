package com.umbook.backend.exception;

public class UsuarioDeshabilitadoException extends RuntimeException {
    public UsuarioDeshabilitadoException() {
        super("El usuario se encuentra deshabilitado y no puede iniciar sesión.");
    }
}
