package ua.nupp.kpp.client.model;

/** Клієнтська модель пункту спостереження (лише дані для відображення). */
public class ObservationPoint {
    private int id;
    private String code;
    private String name;
    private String waterBody;
    private WaterUse waterUse;
    private int sensitivity;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWaterBody() { return waterBody; }
    public void setWaterBody(String waterBody) { this.waterBody = waterBody; }
    public WaterUse getWaterUse() { return waterUse; }
    public void setWaterUse(WaterUse waterUse) { this.waterUse = waterUse; }
    public int getSensitivity() { return sensitivity; }
    public void setSensitivity(int sensitivity) { this.sensitivity = sensitivity; }

    @Override
    public String toString() {
        return code + " — " + name;
    }
}
