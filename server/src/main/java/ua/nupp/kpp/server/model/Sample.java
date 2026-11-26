package ua.nupp.kpp.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Проба води (таблиця sample) — об'єкт чотиристанного життєвого циклу. */
public class Sample {
    private int id;
    private int pointId;
    private LocalDateTime sampledAt;
    private String collector;
    private SampleState state = SampleState.REGISTERED;
    private BigDecimal integralIndex; // може бути null, поки показник не обчислено

    public Sample() {
    }

    public Sample(int id, int pointId, LocalDateTime sampledAt, String collector,
                  SampleState state, BigDecimal integralIndex) {
        this.id = id;
        this.pointId = pointId;
        this.sampledAt = sampledAt;
        this.collector = collector;
        this.state = state;
        this.integralIndex = integralIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPointId() { return pointId; }
    public void setPointId(int pointId) { this.pointId = pointId; }
    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
    public String getCollector() { return collector; }
    public void setCollector(String collector) { this.collector = collector; }
    public SampleState getState() { return state; }
    public void setState(SampleState state) { this.state = state; }
    public BigDecimal getIntegralIndex() { return integralIndex; }
    public void setIntegralIndex(BigDecimal integralIndex) { this.integralIndex = integralIndex; }
}
