package edu.eci.aurora.mapper;

import edu.eci.aurora.model.dto.request.TicketRequestDTO;
import edu.eci.aurora.model.dto.response.TicketResponseDTO;
import edu.eci.aurora.model.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "responseTarget", ignore = true)
    @Mapping(target = "resolutionTarget", ignore = true)
    @Mapping(target = "responseDeadline", ignore = true)
    @Mapping(target = "resolutionDeadline", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Ticket toEntity(TicketRequestDTO dto);

    @Mapping(target = "team", ignore = true)
    @Mapping(target = "sla", ignore = true)
    TicketResponseDTO toDto(Ticket entity);
}
