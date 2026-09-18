package edu.eci.aurora.model.dto.response;

import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response with ticket data")
public class TicketResponseDTO {

    @Schema(description = "Ticket ID")
    private UUID id;

    @Schema(description = "Channel", example = "EMAIL")
    private Source source;

    @Schema(description = "Ticket text")
    private String text;

    @Schema(description = "Category", example = "Acceso y Cuentas")
    private Category category;

    @Schema(description = "Severity", example = "Crítica")
    private Severity severity;

    @Schema(description = "Destination team derived by the API", example = "Identity")
    private Team team;

    @Schema(description = "SLA derived by the API")
    private SlaResponseDTO sla;

    @Schema(description = "Draft response")
    private String draftResponse;

    @Schema(description = "Reception instant", example = "2026-09-18T08:00:00Z")
    private Instant receivedAt;

    @Schema(description = "Persistence instant", example = "2026-09-18T08:00:05Z")
    private Instant createdAt;
}
