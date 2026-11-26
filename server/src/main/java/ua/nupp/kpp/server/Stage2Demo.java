package ua.nupp.kpp.server;

import ua.nupp.kpp.server.db.DbConnection;
import ua.nupp.kpp.server.model.*;
import ua.nupp.kpp.server.service.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

/**
 * Ручна перевірка серверної логіки на реальній MySQL (запускається як звичайна програма).
 * Усі створені записи наприкінці видаляються, тож базу можна не перезаливати.
 */
public class Stage2Demo {
    public static void main(String[] args) throws Exception {
        ObservationPointService points = new ObservationPointService();
        SampleService samples = new SampleService();
        MeasurementService measurements = new MeasurementService();
        InspectionService inspections = new InspectionService();

        System.out.println("=== 1. CRUD пункту спостереження ===");
        ObservationPoint p = new ObservationPoint();
        p.setCode("DEMO-" + (System.currentTimeMillis() % 100000));
        p.setName("Демонстраційний пункт");
        p.setWaterBody("р. Псел");
        p.setWaterUse(WaterUse.RECREATION);
        p.setSensitivity(80);
        p = points.create(p);
        System.out.println("Створено: id=" + p.getId() + ", " + p.getCode());
        p.setName("Демонстраційний пункт (змінено)");
        p = points.update(p);
        System.out.println("Змінено: " + p.getName());

        System.out.println("=== 2. Валідація ===");
        try {
            ObservationPoint bad = new ObservationPoint(0, "X1", "Назва", "Водойма", WaterUse.DRINKING, 150);
            points.create(bad);
        } catch (ValidationException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }
        try {
            samples.create(new Sample(0, 9999, LocalDateTime.now().minusDays(1), "Іванов І. І.", null, null));
        } catch (ValidationException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }

        System.out.println("=== 3. Життєвий цикл проби та інтегральний показник ===");
        Sample s = samples.create(new Sample(0, p.getId(), LocalDateTime.now().minusHours(2), "Іванов І. І.", null, null));
        System.out.println("Проба id=" + s.getId() + ", стан " + s.getState());
        try {
            samples.changeState(s.getId(), SampleState.VALIDATED);
        } catch (ValidationException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }
        measurements.create(new Measurement(0, s.getId(), "Нітрати", new BigDecimal("52"), "мг/дм3", new BigDecimal("45")));
        measurements.create(new Measurement(0, s.getId(), "Фосфати", new BigDecimal("0.9"), "мг/дм3", new BigDecimal("0.6")));
        System.out.println("Показник (попередньо) = " + samples.calculateIndex(s.getId()) + "  (очікується 48.75: F1=50, F2=25, F3=80)");
        inspections.create(new Inspection(0, s.getId(), "Бондаренко Л. М.", LocalDateTime.now().minusHours(1),
                InspectionResult.PASSED, "Демонстраційна перевірка"));

        s = samples.changeState(s.getId(), SampleState.VALIDATED);
        System.out.println("→ " + s.getState());
        s = samples.changeState(s.getId(), SampleState.ANALYZED);
        System.out.println("→ " + s.getState() + ", збережений показник = " + s.getIntegralIndex());
        try {
            samples.changeState(s.getId(), SampleState.REGISTERED);
        } catch (ValidationException e) {
            System.out.println("Заборонений перехід відхилено: " + e.getMessage());
        }
        s = samples.changeState(s.getId(), SampleState.ARCHIVED);
        System.out.println("→ " + s.getState());
        try {
            measurements.create(new Measurement(0, s.getId(), "Хлориди", new BigDecimal("10"), "мг/дм3", new BigDecimal("250")));
        } catch (ValidationException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }

        System.out.println("=== 4. Очищення демонстраційних даних ===");
        try (Connection c = DbConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM sample WHERE id = ?")) {
                ps.setInt(1, s.getId());
                ps.executeUpdate();
            }
        }
        points.delete(p.getId());
        System.out.println("Демонстраційні записи видалено. Пунктів у БД: " + points.list().size());
    }
}
