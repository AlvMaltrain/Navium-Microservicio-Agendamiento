package com.navium.agendamiento.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarNotFound(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "NO_ENCONTRADO",
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> manejarErroresValidacion(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "ERROR_DE_VALIDACION",
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarErroresIntegridad(DataIntegrityViolationException ex) {
        Throwable root = ex.getRootCause();
        String mensaje = (root != null && root.getMessage() != null) ? root.getMessage() : ex.getMessage();
        String codigo = "VIOLACION_DE_CONSTRAINT";
        HttpStatus estado = HttpStatus.CONFLICT;

        if (mensaje != null && mensaje.toLowerCase().contains("usuario_id")) {
            mensaje = "El usuario especificado no existe o la referencia es inválida.";
            codigo = "FK_USUARIO_INVALIDO";
            estado = HttpStatus.BAD_REQUEST;
        } else if (mensaje != null && mensaje.toLowerCase().contains("contenedor_id")) {
            mensaje = "El contenedor especificado no existe o la referencia es inválida.";
            codigo = "FK_CONTENEDOR_INVALIDO";
            estado = HttpStatus.CONFLICT;
        } else if (mensaje != null && mensaje.toLowerCase().contains("foreign key")) {
            codigo = "FK_INVALIDO";
            estado = HttpStatus.CONFLICT;
        } else {
            codigo = "ERROR_DE_INTEGRIDAD";
            estado = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse error = new ErrorResponse(
            estado.value(),
            codigo,
            mensaje
        );
        return new ResponseEntity<>(error, estado);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionesDTO(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}