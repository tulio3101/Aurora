package edu.eci.aurora.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import edu.eci.aurora.model.dto.response.SlaResponseDTO;
import edu.eci.aurora.model.entity.enums.Severity;

class SlaCalculatorImplTest {

    private final SlaCalculator slaCalculator = new SlaCalculatorImpl();

    private static final Instant FRIDAY = Instant.parse("2026-09-18T08:00:00Z");

    @Test
    void calculate_WhenCritica_ShouldReturnOneHourAndFourHours() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.CRITICA, FRIDAY);

        assertEquals("1 hora", sla.getResponseTarget());
        assertEquals("4 horas", sla.getResolutionTarget());
        assertEquals(Instant.parse("2026-09-18T09:00:00Z"), sla.getResponseDeadline());
        assertEquals(Instant.parse("2026-09-18T12:00:00Z"), sla.getResolutionDeadline());
    }

    @Test
    void calculate_WhenAlta_ShouldReturnFourHoursAndTwentyFourHours() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.ALTA, FRIDAY);

        assertEquals("4 horas", sla.getResponseTarget());
        assertEquals("24 horas", sla.getResolutionTarget());
        assertEquals(Instant.parse("2026-09-18T12:00:00Z"), sla.getResponseDeadline());
        assertEquals(Instant.parse("2026-09-19T08:00:00Z"), sla.getResolutionDeadline());
    }

    @Test
    void calculate_WhenMedia_ShouldReturnFourHoursAndSeventyTwoHours() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.MEDIA, FRIDAY);

        assertEquals("4 horas", sla.getResponseTarget());
        assertEquals("72 horas", sla.getResolutionTarget());
        assertEquals(Instant.parse("2026-09-18T12:00:00Z"), sla.getResponseDeadline());
        assertEquals(Instant.parse("2026-09-21T08:00:00Z"), sla.getResolutionDeadline());
    }

    @Test
    void calculate_WhenBajaOnFriday_ShouldSkipWeekends() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.BAJA, FRIDAY);

        assertEquals("24 horas", sla.getResponseTarget());
        assertEquals("10 días hábiles", sla.getResolutionTarget());
        assertEquals(Instant.parse("2026-09-19T08:00:00Z"), sla.getResponseDeadline());
        assertEquals(Instant.parse("2026-10-02T08:00:00Z"), sla.getResolutionDeadline());
    }

    @Test
    void calculate_WhenBajaOnSaturday_ShouldStartCountingOnMonday() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.BAJA, Instant.parse("2026-09-19T10:00:00Z"));

        assertEquals(Instant.parse("2026-10-02T10:00:00Z"), sla.getResolutionDeadline());
    }

    @Test
    void calculate_WhenBaja_ShouldNeverLandOnWeekend() {
        for (int day = 14; day <= 20; day++) {
            Instant start = Instant.parse(String.format("2026-09-%02dT08:00:00Z", day));

            DayOfWeek resolutionDay = slaCalculator.calculate(Severity.BAJA, start)
                .getResolutionDeadline().atZone(ZoneOffset.UTC).getDayOfWeek();

            assertNotEquals(DayOfWeek.SATURDAY, resolutionDay);
            assertNotEquals(DayOfWeek.SUNDAY, resolutionDay);
        }
    }

    @Test
    void calculate_WhenFueraDeAlcance_ShouldReturnNoDeadlines() {
        SlaResponseDTO sla = slaCalculator.calculate(Severity.FUERA_DE_ALCANCE, FRIDAY);

        assertEquals("Redirigir", sla.getResponseTarget());
        assertEquals("No aplica", sla.getResolutionTarget());
        assertNull(sla.getResponseDeadline());
        assertNull(sla.getResolutionDeadline());
    }

    @Test
    void calculate_ForEverySeverity_ShouldReturnSla() {
        for (Severity severity : Severity.values()) {
            assertNotNull(slaCalculator.calculate(severity, FRIDAY));
        }
    }
}
