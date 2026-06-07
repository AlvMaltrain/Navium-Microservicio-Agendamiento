package com.navium.agendamiento.controller;
 
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.navium.agendamiento.dto.AgendamientoRequestDTO;
import com.navium.agendamiento.dto.AgendamientoResponseDTO;
import com.navium.agendamiento.model.Agendamiento;
import com.navium.agendamiento.model.EstadoAgendamiento;
import com.navium.agendamiento.service.AgendamientoService;
 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
 
@RestController
@RequestMapping("/api/agendamientos")
@Tag(name = "Agendamientos", description = "Operaciones CRUD y de negocio sobre agendamientos")
public class AgendamientoController {
 
    private final AgendamientoService servicio;
 
    public AgendamientoController(AgendamientoService servicio) {
        this.servicio = servicio;
    }
    
    //---CREAR AGENDAMIENTO---
    @PostMapping
    @Operation(summary = "Crear agendamiento", description = "Crea un nuevo agendamiento para un camión")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Agendamiento creado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<AgendamientoResponseDTO> crearAgendamiento(@Valid @RequestBody AgendamientoRequestDTO peticion) {
    AgendamientoResponseDTO nuevoAgendamiento = servicio.crearAgendamiento(peticion);
    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAgendamiento);
}
    
    //---ACTUALIZAR ESTADO---
    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de un agendamiento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "400", description = "Estado inválido"),
        @ApiResponse(responseCode = "404", description = "Agendamiento no encontrado")
    })
    public ResponseEntity<Agendamiento> actualizarEstadoAgendamiento(
        @Parameter(description = "ID del agendamiento", example = "1") @PathVariable Long id,
        @Parameter(description = "Nuevo estado", example = "EN_TRANSITO") @RequestParam String estado) {
        Agendamiento agendamientoActualizado = servicio.actualizarEstado(id, estado);
        return ResponseEntity.ok(agendamientoActualizado);
    }
    
    //---LISTAR TODOS LOS AGENDAMIENTOS---
    @GetMapping
    @Operation(summary = "Listar todos", description = "Obtiene todos los agendamientos del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de agendamientos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Agendamiento.class)))),
        @ApiResponse(responseCode = "204", description = "Sin contenido")
    })
    public ResponseEntity<List<AgendamientoResponseDTO>> listarTodos() {
    return ResponseEntity.ok(
        servicio.obtenerTodos().stream()
            .map(this::toResponseDTO)
            .collect(java.util.stream.Collectors.toList())
    );
    }

    //--- BUSCA AGENDAMIENTOS POR RUT DE CHOFER ---
    @GetMapping("/rut/{rut}")
    @Operation(summary = "Buscar por RUT de chofer", description = "Obtiene agendamientos asociados a un RUT de chofer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamientos encontrados",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Agendamiento.class)))),
        @ApiResponse(responseCode = "404", description = "Sin agendamientos para ese RUT")
    })
    public ResponseEntity<List<AgendamientoResponseDTO>> buscarPorRutChofer(@PathVariable String rut) {
        return ResponseEntity.ok(
            servicio.buscarPorRutChofer(rut).stream()
                .map(this::toResponseDTO)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    //---BUSCA UN AGENDAMIENTO POR ID---
    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID", description = "Obtiene un agendamiento específico por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamiento encontrado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "404", description = "Agendamiento no encontrado")
    })
    public ResponseEntity<AgendamientoResponseDTO> buscarPorId(@PathVariable Long id) {
    return ResponseEntity.ok(toResponseDTO(servicio.obtenerPorId(id)));
    }
    
    //---BUSCA UN AGENDAMIENTO POR ESTADO---
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar por estado", description = "Obtiene agendamientos filtrados por estado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de agendamientos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Agendamiento.class)))),
        @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<List<AgendamientoResponseDTO>> listarPorEstado(@PathVariable EstadoAgendamiento estado) {
    return ResponseEntity.ok(
        servicio.buscarPorEstado(estado).stream()
            .map(this::toResponseDTO)
            .collect(java.util.stream.Collectors.toList())
    );
    }
    
    //---ACTUALIZA UN CAMION---
    @PutMapping("/{id}/transporte")
    @Operation(summary = "Actualizar transporte", description = "Asigna patente y RUT chofer a un agendamiento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transporte actualizado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "404", description = "Agendamiento no encontrado")
    })
    public ResponseEntity<Agendamiento> actualizarTransporte(
        @Parameter(description = "ID del agendamiento", example = "1") @PathVariable Long id,
        @Parameter(description = "Patente del camión", example = "ABCD-1212") @RequestParam String patente,
        @Parameter(description = "RUT del chofer", example = "12345678-9") @RequestParam String rut) {
        return ResponseEntity.ok(servicio.actualizarTransporte(id, patente, rut));
    }
    

    //---CANCELA UN AGENDAMIENTO(SOFT DELETE / CAMBIA EL ESTADO A CANCELADO)---
    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar agendamiento", description = "Cancela un agendamiento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamiento cancelado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Agendamiento no encontrado")
    })
    public ResponseEntity<Agendamiento> cancelarAgendamiento(
        @Parameter(description = "ID del agendamiento", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(servicio.cancelarAgendamiento(id));
    }
    
    //--BUSCA AGENDAMIENTOS POR PATENTE---
    @GetMapping("/patente/{patente}")
    @Operation(summary = "Buscar por patente", description = "Obtiene agendamientos asociados a una patente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamientos encontrados",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Agendamiento.class)))),
        @ApiResponse(responseCode = "404", description = "Sin agendamientos para esa patente")
    })
    public ResponseEntity<List<AgendamientoResponseDTO>> buscarPorPatente(@PathVariable String patente) {
    return ResponseEntity.ok(
        servicio.buscarPorPatente(patente).stream()
            .map(this::toResponseDTO)
            .collect(java.util.stream.Collectors.toList())
    );
    }
    
    //--OBTIENE AGENDAMIENTOS POR RANGO DE FECHAS---
    @GetMapping("/fechas")
    @Operation(summary = "Buscar por rango de fechas", description = "Obtiene agendamientos dentro de un rango de fechas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamientos encontrados",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Agendamiento.class)))),
        @ApiResponse(responseCode = "400", description = "Formato de fecha inválido")
    })
    public ResponseEntity<List<Agendamiento>> buscarPorFechas(
        @Parameter(description = "Fecha inicio (ISO-8601)", example = "2026-05-01T00:00:00") @RequestParam String inicio,
        @Parameter(description = "Fecha fin (ISO-8601)", example = "2026-05-31T23:59:59") @RequestParam String fin) {
        LocalDateTime fechaInicio = LocalDateTime.parse(inicio);
        LocalDateTime fechaFin = LocalDateTime.parse(fin);
        return ResponseEntity.ok(servicio.buscarPorRangoFechas(fechaInicio, fechaFin));
    }
    
    //--CONSULTA AGENDAMIENTO VIGENTE POR PATENTE O ID---
    @GetMapping("/consulta")
    @Operation(summary = "Consulta flexible", description = "Consulta un agendamiento vigente por patente o ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamiento encontrado",
            content = @Content(schema = @Schema(implementation = Agendamiento.class))),
        @ApiResponse(responseCode = "204", description = "Sin agendamiento vigente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<Agendamiento> consultar(
        @Parameter(description = "Patente del camión", example = "ABCD-1212") @RequestParam(required = false) String patente,
        @Parameter(description = "ID del agendamiento", example = "1") @RequestParam(required = false) Long id,
        @Parameter(description = "Momento de consulta (ISO-8601)", example = "2026-05-11T10:00:00") @RequestParam(required = false) String momento) {
 
        LocalDateTime momentoConsulta = null;
 
        if (momento != null && !momento.isBlank()) {
            try {
                momentoConsulta = LocalDateTime.parse(momento);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Formato inválido para 'momento'. Use ISO-8601, ej: 2026-04-20T08:00:00", ex);
            }
        }
 
        Agendamiento agendamiento = servicio.consultarAgendamiento(patente, id, momentoConsulta);
 
        if (agendamiento == null) {
            return ResponseEntity.noContent().build();
        }
 
        return ResponseEntity.ok(agendamiento);
    }

    private AgendamientoResponseDTO toResponseDTO(Agendamiento a) {
        AgendamientoResponseDTO dto = new AgendamientoResponseDTO();
        dto.setId(a.getId());
        dto.setIdUsuario(a.getUsuarioId());
        dto.setCorreoUsuario(a.getCorreoUsuario());
        dto.setPatenteCamion(a.getPatenteCamion());
        dto.setRutChofer(a.getRutChofer());
        dto.setTipoOperacion(a.getTipoOperacion() != null ? a.getTipoOperacion().toString() : null);
        dto.setContenedorId(a.getContenedorId());
        dto.setHoraInicio(a.getBloqueInicio());
        dto.setBloqueFin(a.getBloqueFin());
        dto.setEstadoAgendamiento(a.getEstadoAgendamiento() != null ? a.getEstadoAgendamiento().toString() : null);
        return dto;
    }
}
