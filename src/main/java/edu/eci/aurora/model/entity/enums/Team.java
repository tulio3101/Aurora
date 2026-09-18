package edu.eci.aurora.model.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import edu.eci.aurora.exception.InvalidTeamException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@Schema(description = "Destination team, derived from the category")
public enum Team {

    FINANZAS("Finanzas"),
    IDENTITY("Identity"),
    PLATAFORMA("Plataforma"),
    INTEGRACIONES("Integraciones"),
    DATOS("Datos"),
    PRODUCTO("Producto");

    @JsonValue
    private final String label;

    public static Team from(String value) {
        return Arrays.stream(values())
                .filter(t -> t.label.equalsIgnoreCase(value.trim()) || t.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidTeamException("Equipo inválido: '" + value + "'. Valores permitidos: " + labels()));
    }

    public static String labels() {
        return Arrays.stream(values()).map(Team::getLabel).reduce((a, b) -> a + ", " + b).orElse("");
    }
}
