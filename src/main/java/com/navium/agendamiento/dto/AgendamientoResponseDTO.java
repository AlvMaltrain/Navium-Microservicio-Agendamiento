package com.navium.agendamiento.dto;

import java.time.LocalDateTime;

public class AgendamientoResponseDTO {
    private Long id;
    private Long idUsuario;        // ← mapea desde usuarioId de la entidad
    private String correoUsuario;
    private String patenteCamion;
    private String rutChofer;
    private String tipoOperacion;
    private Long contenedorId;
    private LocalDateTime horaInicio;   // ← mapea desde bloqueInicio
    private LocalDateTime bloqueFin;
    private String estadoAgendamiento;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }
    public String getPatenteCamion() { return patenteCamion; }
    public void setPatenteCamion(String patenteCamion) { this.patenteCamion = patenteCamion; }
    public String getRutChofer() { return rutChofer; }
    public void setRutChofer(String rutChofer) { this.rutChofer = rutChofer; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }
    public Long getContenedorId() { return contenedorId; }
    public void setContenedorId(Long contenedorId) { this.contenedorId = contenedorId; }
    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalDateTime getBloqueFin() { return bloqueFin; }
    public void setBloqueFin(LocalDateTime bloqueFin) { this.bloqueFin = bloqueFin; }
    public String getEstadoAgendamiento() { return estadoAgendamiento; }
    public void setEstadoAgendamiento(String estadoAgendamiento) { this.estadoAgendamiento = estadoAgendamiento; }
}