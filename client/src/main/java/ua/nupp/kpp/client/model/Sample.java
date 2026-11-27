package ua.nupp.kpp.client.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Клієнтська модель проби. */
public class Sample {
    private int id;
    private int pointId;
    private LocalDateTime sampledAt;
    private String collector;
    private SampleState state;
    private BigDecimal integralIndex;

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

    @Override
    public String toString() {
        return "Проба №" + id;
    }
}
