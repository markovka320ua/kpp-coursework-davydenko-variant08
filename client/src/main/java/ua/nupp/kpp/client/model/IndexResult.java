package ua.nupp.kpp.client.model;

import java.math.BigDecimal;

/** Розклад інтегрального показника, обчисленого на сервері. */
public class IndexResult {
    private int sampleId;
    private double f1;
    private double f2;
    private double f3;
    private BigDecimal integral;

    public int getSampleId() { return sampleId; }
    public void setSampleId(int sampleId) { this.sampleId = sampleId; }
    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }
    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }
    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }
    public BigDecimal getIntegral() { return integral; }
    public void setIntegral(BigDecimal integral) { this.integral = integral; }
}
