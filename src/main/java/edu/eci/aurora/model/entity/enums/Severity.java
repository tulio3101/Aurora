package edu.eci.aurora.model.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import edu.eci.aurora.exception.InvalidSeverityException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@Schema(description = "Ticket severity, sent by n8n")
public enum Severity {

    CRITICA("Crítica"),
    ALTA("Alta"),
    MEDIA("Media"),
    BAJA("Baja"),
    FUERA_DE_ALCANCE("Fuera de alcance");

    @JsonValue
    private final String label;

    @JsonCreator
    public static Severity from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(value.trim()) || s.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidSeverityException("Severidad inválida: '" + value + "'. Valores permitidos: " + labels()));
    }

    public static String labels() {
        return Arrays.stream(values()).map(Severity::getLabel).reduce((a, b) -> a + ", " + b).orElse("");
    }
}
