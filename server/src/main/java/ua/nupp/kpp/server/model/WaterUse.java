package ua.nupp.kpp.server.model;

/** Тип водокористування пункту спостереження. */
public enum WaterUse {
    DRINKING("Питне водопостачання"),
    RECREATION("Рекреація"),
    FISHERY("Рибне господарство"),
    AGRICULTURE("Сільське господарство");

    private final String label;

    WaterUse(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
