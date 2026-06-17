package com.umbook.backend.exception;

public class NombreUsuarioDuplicadoException extends RuntimeException {
    public NombreUsuarioDuplicadoException(String nombreUsuario) {
        super("El nombre de usuario '" + nombreUsuario + "' ya está en uso.");
    }
}
