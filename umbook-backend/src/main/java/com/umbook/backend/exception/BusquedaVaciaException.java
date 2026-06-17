package com.umbook.backend.exception;

public class BusquedaVaciaException extends RuntimeException {
    public BusquedaVaciaException() {
        super("Debe ingresar al menos un criterio de búsqueda (nombre o apellido).");
    }
}
