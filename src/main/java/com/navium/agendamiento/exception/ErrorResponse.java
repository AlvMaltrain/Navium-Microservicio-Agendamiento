package com.navium.agendamiento.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de error")
public class ErrorResponse {

    @Schema(description = "Código de estado HTTP", example = "400")
    private int status;

    @Schema(description = "Tipo de error", example = "ERROR_DE_VALIDACION")
    private String estado;

    @Schema(description = "Mensaje descriptivo del error", example = "El campo patenteCamion no puede estar vacío")
    private String mensaje;

    @Schema(description = "Momento en que ocurrió el error", example = "2026-05-11T16:00:00")
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String estado, String mensaje) {
        this.status = status;
        this.estado = estado;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getEstado() { return estado; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getTimestamp() { return timestamp; }
}