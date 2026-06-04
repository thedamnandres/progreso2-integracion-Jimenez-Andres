# Progreso 2 - Integración de Sistemas
**Estudiante:** Andrés Jiménez  
**Asignatura:** Integración de Sistemas  
**Universidad:** UDLA - Universidad de las Américas

---

## Descripción de la solución

Sistema de integración para Salud360 que automatiza el flujo de registro de citas médicas. Expone una API REST que recibe solicitudes, las valida y las distribuye a los sistemas correspondientes mediante Apache Camel y RabbitMQ.

---

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.2.5
- Apache Camel 4.4.4
- RabbitMQ 3.13 (via Docker)
- Maven

---
## Instrucciones para levantar RabbitMQ

```bash
docker-compose up -d
```

Acceder al panel de administración:
- URL: http://localhost:15672
- Usuario: admin
- Password: admin

---

## Instrucciones para ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación inicia en: http://localhost:8080

---

## Endpoint disponible

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/citas | Registrar una solicitud de cita médica |

---

## Ejemplo de request válido

```json
{
  "idCita": "CITA-1001",
  "paciente": "Ana Torres",
  "correo": "ana.torres@email.com",
  "especialidad": "Cardiología",
  "fechaCita": "2026-06-15",
  "sede": "Centro Norte",
  "valor": 45.50
}
```

Respuesta esperada (`200 OK`):
```json
{
  "idCita": "CITA-1001",
  "mensaje": "Cita registrada correctamente",
  "status": "ok"
}
```
![Captura de pantalla 2026-06-03 a la(s) 20.16.55.png](docs/capturas/Captura%20de%20pantalla%202026-06-03%20a%20la%28s%29%2020.16.55.png)

---

## Ejemplo de request inválido

```json
{
  "idCita": "",
  "paciente": "",
  "correo": "ana.torres@email.com",
  "especialidad": "Cardiología",
  "fechaCita": "2026-06-15",
  "sede": "Centro Norte",
  "valor": 0
}
```

Respuesta esperada (`400 Bad Request`):
```json
{
  "status": "error",
  "motivo": [
    "idCita es obligatorio",
    "paciente es obligatorio",
    "valor debe ser mayor a 0"
  ]
}
```

![Captura de pantalla 2026-06-03 a la(s) 20.16.20.png](docs/capturas/Captura%20de%20pantalla%202026-06-03%20a%20la%28s%29%2020.16.20.png)

---

## Patrones de integración aplicados

### Point-to-Point
Aplicado en la ruta `direct:facturacion`. El mensaje de facturación se envía a `billing.queue`, una cola dedicada que garantiza que solo el sistema de facturación procese cada solicitud exactamente una vez.

### Publish/Subscribe
Aplicado en la ruta `direct:eventos`. El evento `CITA_CONFIRMADA` se publica en el exchange `appointments.events` (tipo fanout), el cual distribuye el mismo mensaje a `notifications.queue` y `analytics.queue` simultáneamente.

### Transferencia de archivos
Aplicado en la ruta `direct:csv`. Cada cita válida genera una línea en `data/outbox/auditoria-citas.csv`, permitiendo la integración con el sistema legado de auditoría que no tiene API.

### Manejo de errores
Las solicitudes inválidas son rechazadas por `CitaValidationService` y registradas en `data/errors/citas-rechazadas.log` con fecha, hora y detalle del payload rechazado.

---

## Evidencia de funcionamiento

- API responde `200 OK` ante request válido
- API responde `400 Bad Request` ante request inválido
- `billing.queue` recibe mensaje Point-to-Point
- `notifications.queue` y `analytics.queue` reciben evento Pub/Sub
- `data/outbox/auditoria-citas.csv` contiene línea por cada cita válida
- `data/errors/citas-rechazadas.log` registra solicitudes rechazadas