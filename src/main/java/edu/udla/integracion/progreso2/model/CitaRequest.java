package edu.udla.integracion.progreso2.model;

public class CitaRequest {
    private String idCita;
    private String paciente;
    private String correo;
    private String especialidad;
    private String fechaCita;
    private String sede;
    private double valor;

    public String getIdCita() { return idCita; }
    public void setIdCita(String idCita) { this.idCita = idCita; }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getFechaCita() { return fechaCita; }
    public void setFechaCita(String fechaCita) { this.fechaCita = fechaCita; }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}