package edu.eci.aurora.service;

import edu.eci.aurora.model.dto.response.SlaResponseDTO;
import edu.eci.aurora.model.entity.enums.Severity;

import java.time.Instant;

public interface SlaCalculator {

    SlaResponseDTO calculate(Severity severity, Instant receivedAt);
}
