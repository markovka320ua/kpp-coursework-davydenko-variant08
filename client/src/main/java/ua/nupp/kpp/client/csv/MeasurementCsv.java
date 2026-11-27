package ua.nupp.kpp.client.csv;

import ua.nupp.kpp.client.model.Measurement;

import java.math.BigDecimal;

/**
 * Формат CSV для імпорту вимірювань: UTF-8, роздільник «кома», десятковий роздільник «крапка»,
 * перший рядок — заголовок sample_id,parameter,value,unit,norm_limit.
 * Предметна валідація значень виконується на сервері.
 */
public final class MeasurementCsv {
    public static final String HEADER = "sample_id,parameter,value,unit,norm_limit";
    public static final int FIELDS = 5;

    private MeasurementCsv() {
    }

    /** Перевіряє заголовок (BOM на початку ігнорується). */
    public static void checkHeader(String line) {
        String h = line == null ? "" : line.replace("\uFEFF", "").trim();
        if (!HEADER.equals(h)) {
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: " + HEADER);
        }
    }

    /** Розбирає один рядок даних; при помилці формату кидає IllegalArgumentException. */
    public static Measurement parseLine(String line) {
        String[] p = line.split(",", -1);
        if (p.length != FIELDS) {
            throw new IllegalArgumentException("очікується " + FIELDS + " полів, отримано " + p.length);
        }
        int sampleId;
        try {
            sampleId = Integer.parseInt(p[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("sample_id має бути цілим числом");
        }
        return new Measurement(0, sampleId, p[1].trim(), decimal(p[2], "value"), p[3].trim(), decimal(p[4], "norm_limit"));
    }

    private static BigDecimal decimal(String s, String field) {
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("некоректне число в полі " + field);
        }
    }
}
