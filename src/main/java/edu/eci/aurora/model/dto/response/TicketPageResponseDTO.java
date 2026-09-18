package edu.eci.aurora.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Page of tickets, sorted by receivedAt desc")
public class TicketPageResponseDTO {

    @Schema(description = "Tickets in this page")
    private List<TicketResponseDTO> content;

    @Schema(description = "Page number", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total tickets matching the filters", example = "137")
    private long totalElements;

    @Schema(description = "Total pages", example = "7")
    private int totalPages;
}
