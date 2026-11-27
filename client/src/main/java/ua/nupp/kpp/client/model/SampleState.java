package ua.nupp.kpp.client.model;

/** Стани проби (дублюють серверні назви; перевірка переходів виконується лише на сервері). */
public enum SampleState {
    REGISTERED("Зареєстровано"),
    VALIDATED("Валідовано"),
    ANALYZED("Проаналізовано"),
    ARCHIVED("Архівовано");

    private final String label;

    SampleState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
