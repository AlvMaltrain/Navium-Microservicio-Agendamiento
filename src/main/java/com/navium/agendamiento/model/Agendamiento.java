package com.navium.agendamiento.model;
 
import java.time.LocalDateTime;
 
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
 
@Entity
@Table(name = "agendamientos", schema = "navium")
@Schema(description = "Representa un agendamiento de camión en el puerto")
public class Agendamiento {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador interno", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
 
    @Column(name = "usuario_id", nullable = false)
    @Schema(description = "ID del usuario que creó el agendamiento", example = "95")
    private Long usuarioId;
 
    @Column(name = "correo_usuario", nullable = false, length = 100)
    @Schema(description = "Correo del usuario", example = "chofer@puerto.cl")
    private String correoUsuario;
 
    @Column(name = "patente_camion", nullable = false, length = 10)
    @Schema(description = "Patente del camión", example = "ABCD-1212")
    private String patenteCamion;
 
    @Column(name = "rut_chofer", nullable = false, length = 12)
    @Schema(description = "RUT del chofer", example = "12345678-9")
    private String rutChofer;
 
    @Column(name = "tipo_operacion", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de operación del agendamiento")
    private TipoOperacion tipoOperacion;
 
    @Column(name = "contenedor_id")
    @Schema(description = "ID del contenedor asociado (opcional)", example = "86", nullable = true)
    private Long contenedorId;
 
    @Column(name = "bloque_inicio", nullable = false)
    @Schema(description = "Fecha y hora de inicio del bloque", example = "2026-05-11T10:00:00")
    private LocalDateTime bloqueInicio;
 
    @Column(name = "bloque_fin", nullable = false)
    @Schema(description = "Fecha y hora de fin del bloque", example = "2026-05-11T11:00:00")
    private LocalDateTime bloqueFin;
 
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del agendamiento", example = "CREADO")
    private EstadoAgendamiento estado = EstadoAgendamiento.CREADO;
 
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }
    public String getPatenteCamion() { return patenteCamion; }
    public void setPatenteCamion(String patenteCamion) { this.patenteCamion = patenteCamion; }
    public String getRutChofer() { return rutChofer; }
    public void setRutChofer(String rutChofer) { this.rutChofer = rutChofer; }
    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }
    public Long getContenedorId() { return contenedorId; }
    public void setContenedorId(Long contenedorId) { this.contenedorId = contenedorId; }
    public LocalDateTime getBloqueInicio() { return bloqueInicio; }
    public void setBloqueInicio(LocalDateTime bloqueInicio) { this.bloqueInicio = bloqueInicio; }
    public LocalDateTime getBloqueFin() { return bloqueFin; }
    public void setBloqueFin(LocalDateTime bloqueFin) { this.bloqueFin = bloqueFin; }
    public EstadoAgendamiento getEstadoAgendamiento() { return estado; }
    public void setEstadoAgendamiento(EstadoAgendamiento estado) { this.estado = estado; }
}
