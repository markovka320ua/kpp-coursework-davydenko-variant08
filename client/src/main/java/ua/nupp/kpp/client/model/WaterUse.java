package ua.nupp.kpp.client.model;

/** Тип водокористування. */
public enum WaterUse {
    DRINKING("Питне водопостачання"),
    RECREATION("Рекреація"),
    FISHERY("Рибне господарство"),
    AGRICULTURE("Сільське господарство");

    private final String label;

    WaterUse(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
