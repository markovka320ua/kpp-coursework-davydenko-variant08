package ua.nupp.kpp.client.model;

import java.time.LocalDateTime;

/** Клієнтська модель перевірки. */
public class Inspection {
    private int id;
    private int sampleId;
    private String inspector;
    private LocalDateTime checkedAt;
    private InspectionResult result;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSampleId() { return sampleId; }
    public void setSampleId(int sampleId) { this.sampleId = sampleId; }
    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    public InspectionResult getResult() { return result; }
    public void setResult(InspectionResult result) { this.result = result; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
