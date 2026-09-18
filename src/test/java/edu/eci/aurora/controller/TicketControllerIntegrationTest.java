package edu.eci.aurora.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.eci.aurora.repository.TicketRepository;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:aurora;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    private static final String CRITICAL_TICKET = """
        {
          "source": "EMAIL",
          "text": "Desde esta mañana nadie de mi equipo puede iniciar sesión, la página de login se queda cargando indefinidamente",
          "category": "Acceso y Cuentas",
          "severity": "Crítica",
          "receivedAt": "2026-09-18T08:00:00Z",
          "draftResponse": "Estamos revisando el incidente de acceso"
        }
        """;

    @BeforeEach
    void cleanDatabase() {
        ticketRepository.deleteAll();
    }

    @Test
    void createTicket_WithVerificationCase_ShouldDeriveIdentityAndCriticalSla() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CRITICAL_TICKET))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.source").value("EMAIL"))
            .andExpect(jsonPath("$.category").value("Acceso y Cuentas"))
            .andExpect(jsonPath("$.severity").value("Crítica"))
            .andExpect(jsonPath("$.team").value("Identity"))
            .andExpect(jsonPath("$.sla.responseTarget").value("1 hora"))
            .andExpect(jsonPath("$.sla.resolutionTarget").value("4 horas"))
            .andExpect(jsonPath("$.sla.responseDeadline").value("2026-09-18T09:00:00Z"))
            .andExpect(jsonPath("$.sla.resolutionDeadline").value("2026-09-18T12:00:00Z"))
            .andExpect(jsonPath("$.receivedAt").value("2026-09-18T08:00:00Z"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createTicket_WhenOutOfScope_ShouldReturnNoDeadlines() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "source": "CHAT",
                      "text": "¿Me recomiendan un proveedor de internet?",
                      "category": "Interfaz / uso",
                      "severity": "Fuera de alcance",
                      "receivedAt": "2026-09-18T08:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.team").value("Producto"))
            .andExpect(jsonPath("$.sla.responseTarget").value("Redirigir"))
            .andExpect(jsonPath("$.sla.resolutionTarget").value("No aplica"))
            .andExpect(jsonPath("$.sla.responseDeadline").value(nullValue()))
            .andExpect(jsonPath("$.sla.resolutionDeadline").value(nullValue()));
    }

    @Test
    void getTicketById_ShouldReturnPersistedTicket() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CRITICAL_TICKET))
            .andExpect(status().isCreated())
            .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/tickets/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.team").value("Identity"));
    }

    @Test
    void searchTickets_ShouldFilterByTeamAndSortByReceivedAtDesc() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CRITICAL_TICKET)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "source": "WEB_FORM",
                  "text": "La factura de agosto está duplicada",
                  "category": "Facturación",
                  "severity": "Media",
                  "receivedAt": "2026-09-17T10:00:00Z"
                }
                """)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tickets").param("team", "Identity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].team").value("Identity"));

        mockMvc.perform(get("/api/v1/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].receivedAt").value("2026-09-18T08:00:00Z"))
            .andExpect(jsonPath("$.content[1].receivedAt").value("2026-09-17T10:00:00Z"));
    }
}
