package com.navium.agendamiento.dto;

import com.navium.agendamiento.model.TipoOperacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgendamientoRequestDTO {
    @NotNull(message = "El id del usuario no puede estar vacío")
    private Long idUsuario;
    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "Debe ingresar un formato de correo válido")
    private String correoUsuario;
    @NotBlank(message = "La patente del camión es obligatoria")
    private String patenteCamion;
    @NotBlank(message = "El RUT del chofer es obligatorio")
    private String rutChofer;
    @NotBlank(message = "El bloque de inicio es obligatorio")
    private String horaInicio;
    @NotNull(message = "El tipo de operación es obligatorio")
    private TipoOperacion tipoOperacion;
    private Long contenedorId;

    public AgendamientoRequestDTO() {}

    public AgendamientoRequestDTO(Long idUsuario, String correoUsuario, String patenteCamion, String rutChofer, String horaInicio, TipoOperacion tipoOperacion) {
        this.idUsuario = idUsuario;
        this.correoUsuario = correoUsuario;
        this.patenteCamion = patenteCamion;
        this.rutChofer = rutChofer;
        this.horaInicio = horaInicio;
        this.tipoOperacion = tipoOperacion;
    }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public String getPatenteCamion() { return patenteCamion; }
    public void setPatenteCamion(String patenteCamion) { this.patenteCamion = patenteCamion; }

    public String getRutChofer() { return rutChofer; }
    public void setRutChofer(String rutChofer) { this.rutChofer = rutChofer; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public Long getContenedorId() { return contenedorId; }
    public void setContenedorId(Long contenedorId) { this.contenedorId = contenedorId; }  
}