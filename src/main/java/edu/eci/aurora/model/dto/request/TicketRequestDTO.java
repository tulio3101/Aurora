package edu.eci.aurora.model.dto.request;

import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to register a classified ticket")
public class TicketRequestDTO {

    @NotNull
    @Schema(description = "Channel", example = "EMAIL")
    private Source source;

    @NotBlank
    @Schema(description = "Ticket text", example = "Desde esta mañana nadie de mi equipo puede iniciar sesión")
    private String text;

    @NotNull
    @Schema(description = "Category decided by n8n", example = "Acceso y Cuentas")
    private Category category;

    @NotNull
    @Schema(description = "Severity decided by n8n", example = "Crítica")
    private Severity severity;

    @Schema(description = "Reception instant in UTC; defaults to now", example = "2026-09-18T08:00:00Z")
    private Instant receivedAt;

    @Schema(description = "Draft response suggested by the LLM")
    private String draftResponse;
}
