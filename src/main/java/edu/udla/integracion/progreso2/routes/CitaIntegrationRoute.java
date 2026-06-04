package edu.udla.integracion.progreso2.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class CitaIntegrationRoute extends RouteBuilder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure() throws Exception {

        // ── RUTA PRINCIPAL ──────────────────────────────────────────
        from("direct:procesarCita")
                .log(">>> [CITA] Procesando: ${body}")
                .wireTap("direct:facturacion")
                .wireTap("direct:eventos")
                .to("direct:csv");

        // ── POINT-TO-POINT → billing.queue ────────────────────
        from("direct:facturacion")
                .process(exchange -> {
                    CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                    Map<String, Object> msg = Map.of(
                            "idCita", cita.getIdCita(),
                            "paciente", cita.getPaciente(),
                            "especialidad", cita.getEspecialidad(),
                            "valor", cita.getValor(),
                            "tipoMensaje", "COMANDO_FACTURAR_CITA"
                    );
                    exchange.getIn().setBody(mapper.writeValueAsString(msg));
                })
                .to("spring-rabbitmq:direct/billing.queue?routingKey=billing.queue&queues=billing.queue")
                .log(">>> [P2P] Enviado a billing.queue");

        // ── PUBLISH/SUBSCRIBE → appointments.events ───────────
        from("direct:eventos")
                .process(exchange -> {
                    CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                    Map<String, Object> evento = Map.of(
                            "idCita", cita.getIdCita(),
                            "paciente", cita.getPaciente(),
                            "correo", cita.getCorreo(),
                            "especialidad", cita.getEspecialidad(),
                            "fechaCita", cita.getFechaCita(),
                            "sede", cita.getSede(),
                            "tipoEvento", "CITA_CONFIRMADA"
                    );
                    exchange.getIn().setBody(mapper.writeValueAsString(evento));
                })
                .to("spring-rabbitmq:appointments.events?exchangeType=fanout&queues=notifications.queue,analytics.queue&routingKey=")
                .log(">>> [PUB/SUB] Evento publicado en appointments.events");

        // ── CSV LEGADO ─────────────────────────────────────────
        from("direct:csv")
                .process(exchange -> {
                    CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                    String linea = String.format("%s,%s,%s,%s,%s,%s,%.2f%n",
                            cita.getIdCita(), cita.getPaciente(), cita.getCorreo(),
                            cita.getEspecialidad(), cita.getFechaCita(),
                            cita.getSede(), cita.getValor());
                    exchange.getIn().setBody(linea);
                })
                .to("file:data/outbox?fileName=auditoria-citas.csv&fileExist=Append")
                .log(">>> [CSV] Línea escrita en auditoria-citas.csv");

        // ── ERRORES ────────────────────────────────────────────
        from("direct:errorCita")
                .process(exchange -> {
                    Object body = exchange.getIn().getBody();
                    String linea = String.format("[%s] RECHAZO - %s%n",
                            LocalDateTime.now(), body != null ? body.toString() : "payload vacío");
                    exchange.getIn().setBody(linea);
                })
                .to("file:data/errors?fileName=citas-rechazadas.log&fileExist=Append")
                .log(">>> [ERROR] Cita rechazada registrada");
    }
}