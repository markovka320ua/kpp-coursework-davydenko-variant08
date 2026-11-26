package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.model.Measurement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Інтегральний показник стану проби води:
 * I = 0,40·F1 + 0,35·F2 + 0,25·F3, усі фактори нормовані до діапазону 0..100.
 * <ul>
 *   <li>F1 — максимальне відносне перевищення нормативу, у відсотках, обмежене 100;</li>
 *   <li>F2 — кількість критичних параметрів (перевищення від 50 %), кожен дає 25 балів, максимум 100;</li>
 *   <li>F3 — чутливість водокористування пункту спостереження (0..100).</li>
 * </ul>
 * Клас не залежить від БД, тому легко перевіряється модульними тестами.
 */
public final class IndexCalculator {
    public static final BigDecimal W1 = new BigDecimal("0.40");
    public static final BigDecimal W2 = new BigDecimal("0.35");
    public static final BigDecimal W3 = new BigDecimal("0.25");
    /** Поріг критичного перевищення: 50 % понад норматив. */
    public static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("0.5");
    /** Скільки балів F2 дає один критичний параметр. */
    public static final int POINTS_PER_CRITICAL = 25;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private IndexCalculator() {
    }

    /** Відносне перевищення нормативу: (значення − норма) / норма; якщо норму не перевищено — 0. */
    public static BigDecimal exceedance(Measurement m) {
        BigDecimal norm = Validators.requirePositive(m.getNormLimit(), "норматив");
        Validators.requireNonNegative(m.getValue(), "значення");
        BigDecimal e = m.getValue().subtract(norm).divide(norm, 6, RoundingMode.HALF_UP);
        return e.signum() < 0 ? BigDecimal.ZERO : e;
    }

    public static boolean isCritical(Measurement m) {
        return exceedance(m).compareTo(CRITICAL_THRESHOLD) >= 0;
    }

    public static double f1(List<Measurement> measurements) {
        BigDecimal max = BigDecimal.ZERO;
        for (Measurement m : measurements) {
            max = max.max(exceedance(m));
        }
        return max.multiply(HUNDRED).min(HUNDRED).doubleValue();
    }

    public static double f2(List<Measurement> measurements) {
        long critical = measurements.stream().filter(IndexCalculator::isCritical).count();
        return Math.min(100.0, critical * POINTS_PER_CRITICAL);
    }

    public static double f3(int sensitivity) {
        return Validators.requireRange(sensitivity, "чутливість", 0, 100);
    }

    /** Повертає інтегральний показник із двома знаками після коми. */
    public static BigDecimal integral(List<Measurement> measurements, int sensitivity) {
        BigDecimal sum = W1.multiply(BigDecimal.valueOf(f1(measurements)))
                .add(W2.multiply(BigDecimal.valueOf(f2(measurements))))
                .add(W3.multiply(BigDecimal.valueOf(f3(sensitivity))));
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
