package edu.eci.aurora.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.eci.aurora.exception.TicketNotFoundException;
import edu.eci.aurora.model.dto.response.SlaResponseDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import edu.eci.aurora.service.TicketService;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    void createTicket_ShouldReturn201() throws Exception {
        TicketResponseDTO response = TicketResponseDTO.builder()
            .id(UUID.randomUUID())
            .source(Source.EMAIL)
            .text("No puedo iniciar sesión")
            .category(Category.ACCESO_Y_CUENTAS)
            .severity(Severity.CRITICA)
            .team(Team.IDENTITY)
            .sla(new SlaResponseDTO("1 hora", "4 horas",
                Instant.parse("2026-09-18T09:00:00Z"),
                Instant.parse("2026-09-18T12:00:00Z")))
            .receivedAt(Instant.parse("2026-09-18T08:00:00Z"))
            .build();

        when(ticketService.createTicket(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "source": "EMAIL",
                      "text": "No puedo iniciar sesión",
                      "category": "Acceso y Cuentas",
                      "severity": "Crítica",
                      "receivedAt": "2026-09-18T08:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.category").value("Acceso y Cuentas"))
            .andExpect(jsonPath("$.severity").value("Crítica"))
            .andExpect(jsonPath("$.team").value("Identity"))
            .andExpect(jsonPath("$.sla.responseDeadline").value("2026-09-18T09:00:00Z"));
    }

    @Test
    void createTicket_WhenSeverityIsInvalid_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "source": "EMAIL",
                      "text": "El sistema va lento",
                      "category": "Rendimiento",
                      "severity": "Urgente"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("Severidad inválida")));
    }

    @Test
    void createTicket_WhenTextIsBlank_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "source": "EMAIL",
                      "text": "   ",
                      "category": "Rendimiento",
                      "severity": "Alta"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("text")));
    }

    @Test
    void getTicketById_WhenTicketDoesNotExist_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();

        when(ticketService.findTicketById(id))
            .thenThrow(new TicketNotFoundException("Ticket with id: " + id + " not found"));

        mockMvc.perform(get("/api/v1/tickets/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(content().string(Matchers.containsString("not found")));
    }

    @Test
    void searchTickets_WhenTeamFilterIsInvalid_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("team", "Marketing"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("Equipo inválido")));
    }

    @Test
    void searchTickets_WhenSizeIsZero_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("size debe ser al menos 1")));
    }

    @Test
    void searchTickets_WhenPageIsNegative_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("page no puede ser negativo")));
    }

    @Test
    void searchTickets_WhenSizeExceedsLimit_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("size", "100000"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(Matchers.containsString("size no puede exceder 200")));
    }
}
