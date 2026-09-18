# Aurora

API de trazabilidad de triage de tickets de soporte.

Un flujo externo (n8n + LLM) clasifica el ticket y envía **categoría** y **severidad**.
La API no clasifica texto: deriva de forma determinista el **equipo destino** y el **SLA**
(incluidas las fechas límite), persiste el registro y permite consultarlo.

## Stack

Java 21 · Spring Boot 3.3.5 · Spring Data JPA · PostgreSQL · MapStruct · Lombok · springdoc-openapi

## Estructura

```
edu/eci/aurora/
  config/           SwaggerConfig
  controller/       TicketController
  exception/        GlobalExceptionHandler + excepciones de dominio
  mapper/           TicketMapper (MapStruct)
  model/
    dto/request/    TicketRequestDTO
    dto/response/   TicketResponseDTO, SlaResponseDTO, TicketPageResponseDTO
    entity/         Ticket
    entity/enums/   Category, Severity, Team, Source
  repository/       TicketRepository
  service/          TicketService, SlaCalculator, SlaCalculatorImpl
```

## Reglas de negocio

**Categoría → equipo destino**

| Categoría         | Equipo        |
|-------------------|---------------|
| Facturación       | Finanzas      |
| Acceso y Cuentas  | Identity      |
| Rendimiento       | Plataforma    |
| Integraciones     | Integraciones |
| Datos y reportes  | Datos         |
| Interfaz / uso    | Producto      |

**Severidad → SLA**

| Severidad        | Respuesta | Resolución      |
|------------------|-----------|-----------------|
| Crítica          | 1 hora    | 4 horas         |
| Alta             | 4 horas   | 24 horas        |
| Media            | 4 horas   | 72 horas        |
| Baja             | 24 horas  | 10 días hábiles |
| Fuera de alcance | Redirigir | No aplica       |

- `responseDeadline` = `receivedAt` + horas de respuesta.
- `resolutionDeadline` = `receivedAt` + tiempo de resolución.
- **Baja**: los 10 días hábiles saltan sábados y domingos (festivos fuera de alcance por ahora).
- **Fuera de alcance**: sin fechas límite, ambos deadlines en `null`.

Todas las fechas son `Instant` en UTC.

## Ejecución con Docker

```bash
docker compose up --build
```

Levanta PostgreSQL 16 y la API en `localhost:8080`. El servicio `app` espera a que la base
pase su healthcheck (`pg_isready`) antes de arrancar.

## Ejecución local

Requiere JDK 21 y una base PostgreSQL.

```bash
# Variables de entorno
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aurora
export SPRING_DATASOURCE_USERNAME=aurora
export SPRING_DATASOURCE_PASSWORD=aurora

./mvnw spring-boot:run
```

En PowerShell (Windows):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/aurora"
$env:SPRING_DATASOURCE_USERNAME = "aurora"
$env:SPRING_DATASOURCE_PASSWORD = "aurora"

.\mvnw.cmd spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html

## Tests

```bash
./mvnw test
```

Los tests de integración usan H2 en memoria, no requieren PostgreSQL.

## Endpoints

### `POST /api/v1/tickets`

```bash
curl -X POST http://localhost:8080/api/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "source": "EMAIL",
    "text": "Desde esta mañana nadie de mi equipo puede iniciar sesión, la página de login se queda cargando indefinidamente",
    "category": "Acceso y Cuentas",
    "severity": "Crítica",
    "receivedAt": "2026-09-18T08:00:00Z",
    "draftResponse": "Estamos revisando el incidente de acceso"
  }'
```

Respuesta `201`:

```json
{
  "id": "0f6a1c9e-2c1d-4f77-9f5b-3a2b7c8d9e10",
  "source": "EMAIL",
  "text": "Desde esta mañana nadie de mi equipo puede iniciar sesión, la página de login se queda cargando indefinidamente",
  "category": "Acceso y Cuentas",
  "severity": "Crítica",
  "team": "Identity",
  "sla": {
    "responseTarget": "1 hora",
    "resolutionTarget": "4 horas",
    "responseDeadline": "2026-09-18T09:00:00Z",
    "resolutionDeadline": "2026-09-18T12:00:00Z"
  },
  "draftResponse": "Estamos revisando el incidente de acceso",
  "receivedAt": "2026-09-18T08:00:00Z",
  "createdAt": "2026-09-18T08:00:05Z"
}
```

### `GET /api/v1/tickets/{id}`

```bash
curl http://localhost:8080/api/v1/tickets/0f6a1c9e-2c1d-4f77-9f5b-3a2b7c8d9e10
```

### `GET /api/v1/tickets`

Filtros opcionales: `category`, `severity`, `team`, `source`, `from`, `to`, `page`, `size`.
Devuelve una página ordenada por `receivedAt` descendente.

```bash
curl "http://localhost:8080/api/v1/tickets?team=Identity&severity=Cr%C3%ADtica&page=0&size=20"
```

## Errores

Los valores fuera del contrato devuelven `400` con el mensaje de la excepción; un id inexistente
devuelve `404`.

```bash
curl -i -X POST http://localhost:8080/api/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{"source":"EMAIL","text":"El sistema va lento","category":"Rendimiento","severity":"Urgente"}'

# HTTP/1.1 400
# Severidad inválida: 'Urgente'. Valores permitidos: Crítica, Alta, Media, Baja, Fuera de alcance
```
