package ua.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;
import ua.nupp.kpp.server.model.Measurement;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexCalculatorTest {

    private static Measurement m(String value, String norm) {
        return new Measurement(0, 1, "Параметр", new BigDecimal(value), "мг/дм3", new BigDecimal(norm));
    }

    @Test
    void f1IsZeroWhenAllValuesWithinNorm() {
        assertEquals(0.0, IndexCalculator.f1(List.of(m("10", "45"), m("0.3", "0.5"))), 0.0001);
    }

    @Test
    void f1EqualsMaxExceedancePercent() {
        // 0.9 / 0.6 -> перевищення 50 %
        assertEquals(50.0, IndexCalculator.f1(List.of(m("52", "45"), m("0.9", "0.6"))), 0.0001);
    }

    @Test
    void f1IsCappedAtOneHundred() {
        // 1.2 / 0.5 -> перевищення 140 %, обмежується 100
        assertEquals(100.0, IndexCalculator.f1(List.of(m("1.2", "0.5"))), 0.0001);
    }

    @Test
    void parameterAtExactlyFiftyPercentIsCritical() {
        assertTrue(IndexCalculator.isCritical(m("0.9", "0.6")));
    }

    @Test
    void parameterBelowFiftyPercentIsNotCritical() {
        assertFalse(IndexCalculator.isCritical(m("0.8", "0.6")));
    }

    @Test
    void f2CountsCriticalParametersByTwentyFivePoints() {
        // амоній 1.2/0.5 і БСК5 4.8/3.0 — критичні, нітрити 0.09/0.08 — ні
        List<Measurement> list = List.of(m("1.2", "0.5"), m("0.09", "0.08"), m("4.8", "3.0"));
        assertEquals(50.0, IndexCalculator.f2(list), 0.0001);
    }

    @Test
    void f2IsCappedAtOneHundred() {
        List<Measurement> list = List.of(m("10", "1"), m("10", "1"), m("10", "1"), m("10", "1"), m("10", "1"));
        assertEquals(100.0, IndexCalculator.f2(list), 0.0001);
    }

    @Test
    void integralForDemoSampleTwoIs46_25() {
        // F1 = 50, F2 = 25, F3 = 70  ->  0.40*50 + 0.35*25 + 0.25*70
        List<Measurement> list = List.of(m("52", "45"), m("0.9", "0.6"), m("2.4", "3.0"));
        assertEquals(new BigDecimal("46.25"), IndexCalculator.integral(list, 70));
    }

    @Test
    void integralForDemoSampleThreeIs72_50() {
        // F1 = 100, F2 = 50, F3 = 60
        List<Measurement> list = List.of(m("1.2", "0.5"), m("0.09", "0.08"), m("4.8", "3.0"));
        assertEquals(new BigDecimal("72.50"), IndexCalculator.integral(list, 60));
    }

    @Test
    void sensitivityOutOfRangeIsRejected() {
        assertThrows(ValidationException.class, () -> IndexCalculator.f3(101));
        assertThrows(ValidationException.class, () -> IndexCalculator.f3(-1));
    }

    @Test
    void nonPositiveNormIsRejected() {
        assertThrows(ValidationException.class, () -> IndexCalculator.exceedance(m("5", "0")));
    }
}
