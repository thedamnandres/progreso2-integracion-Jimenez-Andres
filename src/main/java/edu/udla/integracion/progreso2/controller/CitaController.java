package edu.udla.integracion.progreso2.controller;

import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaValidationService validationService;

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping
    public ResponseEntity<?> registrarCita(@RequestBody CitaRequest cita) {
        List<String> errores = validationService.validar(cita);

        if (!errores.isEmpty()) {
            producerTemplate.sendBody("direct:errorCita", cita);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "motivo", errores
            ));
        }

        producerTemplate.sendBody("direct:procesarCita", cita);
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensaje", "Cita registrada correctamente",
                "idCita", cita.getIdCita()
        ));
    }
}