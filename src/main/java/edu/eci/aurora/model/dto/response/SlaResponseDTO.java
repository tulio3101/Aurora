package edu.eci.aurora.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "SLA derived by the API")
public class SlaResponseDTO {

    @Schema(description = "Response target", example = "1 hora")
    private String responseTarget;

    @Schema(description = "Resolution target", example = "4 horas")
    private String resolutionTarget;

    @Schema(description = "Response deadline", example = "2026-09-18T09:00:00Z")
    private Instant responseDeadline;

    @Schema(description = "Resolution deadline", example = "2026-09-18T12:00:00Z")
    private Instant resolutionDeadline;
}
