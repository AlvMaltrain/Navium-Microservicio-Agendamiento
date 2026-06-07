package com.navium.agendamiento.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}