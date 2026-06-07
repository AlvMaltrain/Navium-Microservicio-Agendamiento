package com.navium.agendamiento.dto;

public class AgendamientoMensajeDTO {
    private String correoUsuario;
    private String estadoAgendamiento;
    private String patenteCamion;
    private String rutChofer;
    private String horaInicio;
    

//Constructores
public AgendamientoMensajeDTO() {}

public AgendamientoMensajeDTO(String correoUsuario, String estadoAgendamiento, String patenteCamion, String rutChofer, String horaInicio) {
    this.correoUsuario = correoUsuario;
    this.estadoAgendamiento = estadoAgendamiento;
    this.patenteCamion = patenteCamion;
    this.rutChofer = rutChofer;
    this.horaInicio = horaInicio;
}

// Getters y Setters 
    public String getCorreoUsuario() {return correoUsuario;}
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario;}
    public String getEstadoAgendamiento() { return estadoAgendamiento; }
    public void setEstadoAgendamiento(String estadoAgendamiento) { this.estadoAgendamiento = estadoAgendamiento; }
    public String getPatenteCamion() { return patenteCamion; }
    public void setPatenteCamion(String patenteCamion) { this.patenteCamion = patenteCamion; }
    public String getRutChofer() { return rutChofer; }
    public void setRutChofer(String rutChofer) { this.rutChofer = rutChofer; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

}