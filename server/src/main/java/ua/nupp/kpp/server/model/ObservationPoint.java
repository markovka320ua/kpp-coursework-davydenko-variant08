package ua.nupp.kpp.server.model;

/** Пункт спостереження (таблиця observation_point). */
public class ObservationPoint {
    private int id;
    private String code;
    private String name;
    private String waterBody;
    private WaterUse waterUse;
    private int sensitivity; // чутливість водокористування, 0..100 (фактор F3)

    public ObservationPoint() {
    }

    public ObservationPoint(int id, String code, String name, String waterBody,
                            WaterUse waterUse, int sensitivity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.waterBody = waterBody;
        this.waterUse = waterUse;
        this.sensitivity = sensitivity;
    }

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
}
