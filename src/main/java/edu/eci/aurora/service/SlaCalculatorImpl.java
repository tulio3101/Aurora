package edu.eci.aurora.service;

import edu.eci.aurora.model.dto.response.SlaResponseDTO;
import edu.eci.aurora.model.entity.enums.Severity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;

@Service
public class SlaCalculatorImpl implements SlaCalculator {

    private static final int BUSINESS_DAYS_FOR_LOW_SEVERITY = 10;

    private record SlaRule(String responseTarget,
                           String resolutionTarget,
                           Duration responseDuration,
                           Duration resolutionDuration,
                           Integer resolutionBusinessDays) {
    }

    private static final Map<Severity, SlaRule> RULES = new EnumMap<>(Severity.class);

    static {
        RULES.put(Severity.CRITICA, new SlaRule(
                "1 hora", "4 horas", Duration.ofHours(1), Duration.ofHours(4), null));
        RULES.put(Severity.ALTA, new SlaRule(
                "4 horas", "24 horas", Duration.ofHours(4), Duration.ofHours(24), null));
        RULES.put(Severity.MEDIA, new SlaRule(
                "4 horas", "72 horas", Duration.ofHours(4), Duration.ofHours(72), null));
        RULES.put(Severity.BAJA, new SlaRule(
                "24 horas", "10 días hábiles", Duration.ofHours(24), null, BUSINESS_DAYS_FOR_LOW_SEVERITY));
        RULES.put(Severity.FUERA_DE_ALCANCE, new SlaRule(
                "Redirigir", "No aplica", null, null, null));
    }

    @Override
    public SlaResponseDTO calculate(Severity severity, Instant receivedAt) {
        SlaRule rule = RULES.get(severity);

        if (rule.responseDuration() == null) {
            return SlaResponseDTO.builder()
                    .responseTarget(rule.responseTarget())
                    .resolutionTarget(rule.resolutionTarget())
                    .build();
        }

        Instant resolutionDeadline = rule.resolutionBusinessDays() != null
                ? plusBusinessDays(receivedAt, rule.resolutionBusinessDays())
                : receivedAt.plus(rule.resolutionDuration());

        return SlaResponseDTO.builder()
                .responseTarget(rule.responseTarget())
                .resolutionTarget(rule.resolutionTarget())
                .responseDeadline(receivedAt.plus(rule.responseDuration()))
                .resolutionDeadline(resolutionDeadline)
                .build();
    }

    private Instant plusBusinessDays(Instant from, int businessDays) {
        Instant cursor = from;
        int added = 0;

        while (added < businessDays) {
            cursor = cursor.plus(1, ChronoUnit.DAYS);
            if (isBusinessDay(cursor)) {
                added++;
            }
        }

        return cursor;
    }

    private boolean isBusinessDay(Instant instant) {
        return switch (instant.atZone(ZoneOffset.UTC).getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }
}
