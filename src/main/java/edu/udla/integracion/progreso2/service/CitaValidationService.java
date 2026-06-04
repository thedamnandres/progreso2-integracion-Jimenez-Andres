package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaValidationService {

    public List<String> validar(CitaRequest cita) {
        List<String> errores = new ArrayList<>();

        if (cita.getIdCita() == null || cita.getIdCita().isBlank())
            errores.add("idCita es obligatorio");
        if (cita.getPaciente() == null || cita.getPaciente().isBlank())
            errores.add("paciente es obligatorio");
        if (cita.getCorreo() == null || cita.getCorreo().isBlank())
            errores.add("correo es obligatorio");
        if (cita.getEspecialidad() == null || cita.getEspecialidad().isBlank())
            errores.add("especialidad es obligatoria");
        if (cita.getFechaCita() == null || cita.getFechaCita().isBlank())
            errores.add("fechaCita es obligatoria");
        if (cita.getSede() == null || cita.getSede().isBlank())
            errores.add("sede es obligatoria");
        if (cita.getValor() <= 0)
            errores.add("valor debe ser mayor a 0");

        return errores;
    }
}