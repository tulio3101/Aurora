package edu.eci.aurora.model.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.eci.aurora.exception.InvalidSourceException;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "Channel the ticket arrived through")
public enum Source {

    EMAIL,
    CHAT,
    WEB_FORM;

    @JsonCreator
    public static Source from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidSourceException("Canal inválido: '" + value + "'. Valores permitidos: " + labels()));
    }

    public static String labels() {
        return Arrays.stream(values()).map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("");
    }
}
