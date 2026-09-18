package edu.eci.aurora.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.aurora.exception.TicketNotFoundException;
import edu.eci.aurora.mapper.TicketMapper;
import edu.eci.aurora.model.dto.request.TicketRequestDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.Ticket;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import edu.eci.aurora.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private SlaCalculator slaCalculator;

    @InjectMocks
    private TicketService ticketService;

    private static final Instant RECEIVED_AT = Instant.parse("2026-09-18T08:00:00Z");

    @Test
    void createTicket_ShouldDeriveTeamAndSla() {
        TicketRequestDTO request = TicketRequestDTO.builder()
            .source(Source.EMAIL)
            .text("No puedo iniciar sesión")
            .category(Category.ACCESO_Y_CUENTAS)
            .severity(Severity.CRITICA)
            .receivedAt(RECEIVED_AT)
            .build();

        when(ticketMapper.toEntity(request)).thenReturn(Ticket.builder()
            .source(Source.EMAIL)
            .text("No puedo iniciar sesión")
            .category(Category.ACCESO_Y_CUENTAS)
            .severity(Severity.CRITICA)
            .build());

        when(slaCalculator.calculate(Severity.CRITICA, RECEIVED_AT))
            .thenReturn(new edu.eci.aurora.model.dto.response.SlaResponseDTO(
                "1 hora", "4 horas",
                Instant.parse("2026-09-18T09:00:00Z"),
                Instant.parse("2026-09-18T12:00:00Z")));

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(call -> call.getArgument(0));
        when(ticketMapper.toDto(any(Ticket.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO result = ticketService.createTicket(request);

        assertEquals(Team.IDENTITY, result.getTeam());
        assertEquals("1 hora", result.getSla().getResponseTarget());
        assertEquals("4 horas", result.getSla().getResolutionTarget());
        assertEquals(Instant.parse("2026-09-18T09:00:00Z"), result.getSla().getResponseDeadline());
        assertEquals(Instant.parse("2026-09-18T12:00:00Z"), result.getSla().getResolutionDeadline());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_WhenReceivedAtIsNull_ShouldUseNow() {
        TicketRequestDTO request = TicketRequestDTO.builder()
            .source(Source.CHAT)
            .text("Consulta menor")
            .category(Category.FACTURACION)
            .severity(Severity.BAJA)
            .build();

        when(ticketMapper.toEntity(request)).thenReturn(Ticket.builder()
            .category(Category.FACTURACION)
            .severity(Severity.BAJA)
            .build());
        when(slaCalculator.calculate(eq(Severity.BAJA), any(Instant.class)))
            .thenReturn(new edu.eci.aurora.model.dto.response.SlaResponseDTO(
                "24 horas", "10 días hábiles", null, null));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(call -> call.getArgument(0));
        when(ticketMapper.toDto(any(Ticket.class))).thenReturn(new TicketResponseDTO());

        ticketService.createTicket(request);

        verify(slaCalculator).calculate(eq(Severity.BAJA), any(Instant.class));
    }

    @Test
    void findTicketById_WhenTicketDoesNotExist_ShouldThrow() {
        UUID id = UUID.randomUUID();

        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        TicketNotFoundException ex = assertThrows(TicketNotFoundException.class,
            () -> ticketService.findTicketById(id));

        assertTrue(ex.getMessage().contains(id.toString()));
    }
}
