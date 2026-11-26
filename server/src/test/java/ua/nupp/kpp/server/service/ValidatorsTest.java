package ua.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {

    @Test
    void requireTextRejectsBlankValue() {
        assertThrows(ValidationException.class, () -> Validators.requireText("   ", "назва", 10));
        assertThrows(ValidationException.class, () -> Validators.requireText(null, "назва", 10));
    }

    @Test
    void requireTextTrimsAndChecksLength() {
        assertEquals("Ворскла", Validators.requireText("  Ворскла  ", "водойма", 20));
        assertThrows(ValidationException.class, () -> Validators.requireText("дуже довга назва", "назва", 5));
    }

    @Test
    void optionalTextTurnsBlankIntoNull() {
        assertNull(Validators.optionalText("  ", "примітка", 255));
    }

    @Test
    void requireRangeRejectsOutOfRangeValue() {
        assertEquals(100, Validators.requireRange(100, "чутливість", 0, 100));
        assertThrows(ValidationException.class, () -> Validators.requireRange(101, "чутливість", 0, 100));
    }

    @Test
    void requirePositiveRejectsZeroAndNegative() {
        assertThrows(ValidationException.class, () -> Validators.requirePositive(BigDecimal.ZERO, "норматив"));
        assertThrows(ValidationException.class, () -> Validators.requirePositive(new BigDecimal("-1"), "норматив"));
    }

    @Test
    void requireNonNegativeRejectsNegativeValue() {
        assertThrows(ValidationException.class, () -> Validators.requireNonNegative(new BigDecimal("-0.1"), "значення"));
        assertEquals(BigDecimal.ZERO, Validators.requireNonNegative(BigDecimal.ZERO, "значення"));
    }
}
