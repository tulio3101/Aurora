package edu.eci.aurora.model.entity.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.eci.aurora.exception.InvalidCategoryException;
import edu.eci.aurora.exception.InvalidSeverityException;

class CategoryTest {

    @ParameterizedTest
    @CsvSource({
        "Facturación,       Finanzas",
        "Acceso y Cuentas,  Identity",
        "Rendimiento,       Plataforma",
        "Integraciones,     Integraciones",
        "Datos y reportes,  Datos",
        "Interfaz / uso,    Producto"
    })
    void from_ShouldMapEveryCategoryToItsTeam(String categoryLabel, String expectedTeam) {
        assertEquals(expectedTeam, Category.from(categoryLabel).getTeam().getLabel());
    }

    @Test
    void every_CategoryHasTeam() {
        for (Category category : Category.values()) {
            assertNotNull(category.getTeam());
        }
    }

    @Test
    void from_ShouldAlsoAcceptEnumName() {
        assertEquals(Category.ACCESO_Y_CUENTAS, Category.from("ACCESO_Y_CUENTAS"));
        assertEquals(Severity.CRITICA, Severity.from("critica"));
    }

    @Test
    void from_WhenValueIsNotInContract_ShouldThrow() {
        assertThrows(InvalidCategoryException.class, () -> Category.from("Marketing"));
        assertThrows(InvalidSeverityException.class, () -> Severity.from("Urgente"));
    }
}
