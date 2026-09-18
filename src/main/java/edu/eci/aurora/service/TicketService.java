package edu.eci.aurora.service;

import edu.eci.aurora.exception.TicketNotFoundException;
import edu.eci.aurora.mapper.TicketMapper;
import edu.eci.aurora.model.dto.request.TicketRequestDTO;
import edu.eci.aurora.model.dto.response.SlaResponseDTO;
import edu.eci.aurora.model.dto.response.TicketPageResponseDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.Ticket;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import edu.eci.aurora.repository.TicketRepository;
import edu.eci.aurora.repository.TicketSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final SlaCalculator slaCalculator;

    @Transactional
    public TicketResponseDTO createTicket(TicketRequestDTO dto) {

        Ticket ticket = ticketMapper.toEntity(dto);

        Instant receivedAt = dto.getReceivedAt() != null ? dto.getReceivedAt() : Instant.now();
        SlaResponseDTO sla = slaCalculator.calculate(dto.getSeverity(), receivedAt);

        ticket.setReceivedAt(receivedAt);
        ticket.setTeam(dto.getCategory().getTeam());
        ticket.setResponseTarget(sla.getResponseTarget());
        ticket.setResolutionTarget(sla.getResolutionTarget());
        ticket.setResponseDeadline(sla.getResponseDeadline());
        ticket.setResolutionDeadline(sla.getResolutionDeadline());

        Ticket ticketSaved = ticketRepository.save(ticket);

        log.info("Ticket registrado: id={}, category={}, severity={}, team={}",
                ticketSaved.getId(), ticketSaved.getCategory().getLabel(),
                ticketSaved.getSeverity().getLabel(), ticketSaved.getTeam().getLabel());

        return toDto(ticketSaved);
    }

    @Transactional
    public TicketResponseDTO findTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with id: " + id + " not found"));

        return toDto(ticket);
    }

    @Transactional
    public TicketPageResponseDTO searchTickets(Category category, Severity severity, Team team, Source source,
                                               Instant from, Instant to, int page, int size) {

        Specification<Ticket> filters = Specification
            .where(TicketSpecifications.hasCategory(category))
            .and(TicketSpecifications.hasSeverity(severity))
            .and(TicketSpecifications.hasTeam(team))
            .and(TicketSpecifications.hasSource(source))
            .and(TicketSpecifications.receivedFrom(from))
            .and(TicketSpecifications.receivedTo(to));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedAt"));

        Page<Ticket> tickets = ticketRepository.findAll(filters, pageRequest);

        List<TicketResponseDTO> content = tickets.getContent().stream().map(this::toDto).toList();

        return TicketPageResponseDTO.builder()
                .content(content)
                .page(tickets.getNumber())
                .size(tickets.getSize())
                .totalElements(tickets.getTotalElements())
                .totalPages(tickets.getTotalPages())
                .build();
    }

    private TicketResponseDTO toDto(Ticket ticket) {
        TicketResponseDTO dto = ticketMapper.toDto(ticket);

        dto.setTeam(ticket.getTeam());
        dto.setSla(SlaResponseDTO.builder()
                .responseTarget(ticket.getResponseTarget())
                .resolutionTarget(ticket.getResolutionTarget())
                .responseDeadline(ticket.getResponseDeadline())
                .resolutionDeadline(ticket.getResolutionDeadline())
                .build());

        return dto;
    }
}
