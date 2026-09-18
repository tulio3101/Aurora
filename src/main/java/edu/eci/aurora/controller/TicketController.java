package edu.eci.aurora.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.aurora.model.dto.request.TicketRequestDTO;
import edu.eci.aurora.model.dto.response.TicketPageResponseDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import edu.eci.aurora.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Operations related to classified tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("")
    @Operation(summary = "Register a ticket", description = "Persists a ticket already classified by n8n and derives its team and SLA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ticket registered successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    public ResponseEntity<TicketResponseDTO> createTicket(
        @Valid @RequestBody TicketRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(dto));

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a ticket", description = "Returns a ticket by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    public ResponseEntity<TicketResponseDTO> getTicketById(
        @Parameter(description = "Ticket ID") @PathVariable UUID id) {

        return ResponseEntity.ok(ticketService.findTicketById(id));

    }

    @GetMapping("")
    @Operation(summary = "Search tickets", description = "Returns the ticket history filtered and paginated, sorted by receivedAt desc")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Page of tickets",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketPageResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid filter value", content = @Content)
    })
    public ResponseEntity<TicketPageResponseDTO> searchTickets(
        @Parameter(description = "Category", example = "Acceso y Cuentas") @RequestParam(required = false) String category,
        @Parameter(description = "Severity", example = "Crítica") @RequestParam(required = false) String severity,
        @Parameter(description = "Team", example = "Identity") @RequestParam(required = false) String team,
        @Parameter(description = "Source", example = "EMAIL") @RequestParam(required = false) String source,
        @Parameter(description = "Received from", example = "2026-09-01T00:00:00Z") @RequestParam(required = false) Instant from,
        @Parameter(description = "Received to", example = "2026-09-30T23:59:59Z") @RequestParam(required = false) Instant to,
        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "no puede ser negativo") int page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "debe ser al menos 1")
        @Max(value = 200, message = "no puede exceder 200") int size) {

        return ResponseEntity.ok(ticketService.searchTickets(
            category != null ? Category.from(category) : null,
            severity != null ? Severity.from(severity) : null,
            team != null ? Team.from(team) : null,
            source != null ? Source.from(source) : null,
            from, to, page, size));

    }

}
