package ua.nupp.kpp.client.model;

import java.math.BigDecimal;

/** Клієнтська модель вимірювання. */
public class Measurement {
    private int id;
    private int sampleId;
    private String parameter;
    private BigDecimal value;
    private String unit;
    private BigDecimal normLimit;

    public Measurement() {
    }

    public Measurement(int id, int sampleId, String parameter, BigDecimal value, String unit, BigDecimal normLimit) {
        this.id = id;
        this.sampleId = sampleId;
        this.parameter = parameter;
        this.value = value;
        this.unit = unit;
        this.normLimit = normLimit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSampleId() { return sampleId; }
    public void setSampleId(int sampleId) { this.sampleId = sampleId; }
    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getNormLimit() { return normLimit; }
    public void setNormLimit(BigDecimal normLimit) { this.normLimit = normLimit; }
}
