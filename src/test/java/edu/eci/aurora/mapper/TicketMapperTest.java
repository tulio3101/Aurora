package edu.eci.aurora.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.eci.aurora.model.dto.request.TicketRequestDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.Ticket;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;

class TicketMapperTest {

    private final TicketMapper ticketMapper = new TicketMapperImpl();

    @Test
    void toEntity_ShouldMapBaseFieldsAndIgnoreDerivedOnes() {
        TicketRequestDTO dto = TicketRequestDTO.builder()
            .source(Source.EMAIL)
            .text("No puedo iniciar sesión")
            .category(Category.ACCESO_Y_CUENTAS)
            .severity(Severity.CRITICA)
            .receivedAt(Instant.parse("2026-09-18T08:00:00Z"))
            .draftResponse("Estamos revisando tu caso")
            .build();

        Ticket entity = ticketMapper.toEntity(dto);

        assertEquals(Source.EMAIL, entity.getSource());
        assertEquals("No puedo iniciar sesión", entity.getText());
        assertEquals(Category.ACCESO_Y_CUENTAS, entity.getCategory());
        assertEquals(Severity.CRITICA, entity.getSeverity());
        assertEquals(Instant.parse("2026-09-18T08:00:00Z"), entity.getReceivedAt());
        assertEquals("Estamos revisando tu caso", entity.getDraftResponse());

        assertNull(entity.getId());
        assertNull(entity.getTeam());
        assertNull(entity.getResponseTarget());
        assertNull(entity.getResolutionDeadline());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void toDto_ShouldMapBaseFieldsAndIgnoreTeamAndSla() {
        UUID id = UUID.randomUUID();

        Ticket entity = Ticket.builder()
            .id(id)
            .source(Source.CHAT)
            .text("El reporte no exporta")
            .category(Category.DATOS_Y_REPORTES)
            .severity(Severity.MEDIA)
            .team(Team.DATOS)
            .responseTarget("4 horas")
            .resolutionTarget("72 horas")
            .receivedAt(Instant.parse("2026-09-18T08:00:00Z"))
            .createdAt(Instant.parse("2026-09-18T08:00:05Z"))
            .build();

        TicketResponseDTO dto = ticketMapper.toDto(entity);

        assertEquals(id, dto.getId());
        assertEquals(Source.CHAT, dto.getSource());
        assertEquals("El reporte no exporta", dto.getText());
        assertEquals(Category.DATOS_Y_REPORTES, dto.getCategory());
        assertEquals(Severity.MEDIA, dto.getSeverity());
        assertEquals(Instant.parse("2026-09-18T08:00:00Z"), dto.getReceivedAt());
        assertEquals(Instant.parse("2026-09-18T08:00:05Z"), dto.getCreatedAt());

        assertNull(dto.getTeam());
        assertNull(dto.getSla());
    }
}
