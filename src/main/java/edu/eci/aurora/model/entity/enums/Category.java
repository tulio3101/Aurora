package edu.eci.aurora.model.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import edu.eci.aurora.exception.InvalidCategoryException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@Schema(description = "Ticket category, sent by n8n")
public enum Category {

    FACTURACION("Facturación", Team.FINANZAS),
    ACCESO_Y_CUENTAS("Acceso y Cuentas", Team.IDENTITY),
    RENDIMIENTO("Rendimiento", Team.PLATAFORMA),
    INTEGRACIONES("Integraciones", Team.INTEGRACIONES),
    DATOS_Y_REPORTES("Datos y reportes", Team.DATOS),
    INTERFAZ_USO("Interfaz / uso", Team.PRODUCTO);

    @JsonValue
    private final String label;

    private final Team team;

    @JsonCreator
    public static Category from(String value) {
        return Arrays.stream(values())
                .filter(c -> c.label.equalsIgnoreCase(value.trim()) || c.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidCategoryException("Categoría inválida: '" + value + "'. Valores permitidos: " + labels()));
    }

    public static String labels() {
        return Arrays.stream(values()).map(Category::getLabel).reduce((a, b) -> a + ", " + b).orElse("");
    }
}
