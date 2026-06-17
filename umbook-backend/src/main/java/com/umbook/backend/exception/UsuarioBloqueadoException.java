package com.umbook.backend.exception;

public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException() {
        super("Tu cuenta fue bloqueada por 10 intentos fallidos. Volvé a intentarlo en 1 hora.");
    }
}
