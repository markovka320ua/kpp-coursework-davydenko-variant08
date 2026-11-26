package ua.nupp.kpp.server.model;

/** Стани життєвого циклу проби. Значення в БД зберігаються як name(). */
public enum SampleState {
    REGISTERED("Зареєстровано"),
    VALIDATED("Валідовано"),
    ANALYZED("Проаналізовано"),
    ARCHIVED("Архівовано");

    private final String label;

    SampleState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
