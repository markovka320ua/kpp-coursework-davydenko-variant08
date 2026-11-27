package ua.nupp.kpp.client.model;

/** Результат перевірки. */
public enum InspectionResult {
    PASSED("Пройдено"),
    FAILED("Не пройдено");

    private final String label;

    InspectionResult(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
