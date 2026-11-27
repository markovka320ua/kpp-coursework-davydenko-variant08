package ua.nupp.kpp.client.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ua.nupp.kpp.client.api.ApiException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/** Допоміжні методи для побудови інтерфейсу та показу повідомлень. */
public final class Ui {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Ui() {
    }

    /** Дія, що може кинути виняток (звернення до сервера, розбір введення). */
    @FunctionalInterface
    public interface Act {
        void run() throws Exception;
    }

    /** Виконує дію й показує користувачу зрозуміле повідомлення замість аварійного завершення. */
    public static void safe(Act action) {
        try {
            action.run();
        } catch (ApiException | IllegalArgumentException e) {
            error(e.getMessage());
        } catch (Exception e) {
            error("Несподівана помилка: " + e);
        }
    }

    public static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Помилка");
        alert.showAndWait();
    }

    public static void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Повідомлення");
        alert.showAndWait();
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Створює текстову колонку таблиці з функції, що бере значення з рядка. */
    public static <T> TableColumn<T, String> col(String title, Function<T, Object> getter, double width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> {
            Object value = getter.apply(cell.getValue());
            return new ReadOnlyStringWrapper(value == null ? "—" : String.valueOf(value));
        });
        column.setPrefWidth(width);
        return column;
    }

    public static GridPane form() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    public static void addRow(GridPane grid, int row, String label, Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }

    public static BigDecimal decimal(TextField field, String name) {
        String text = field.getText() == null ? "" : field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Поле «" + name + "» є обов'язковим");
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути числом");
        }
    }

    public static int integer(TextField field, String name) {
        String text = field.getText() == null ? "" : field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Поле «" + name + "» є обов'язковим");
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути цілим числом");
        }
    }

    public static LocalDateTime dateTime(DatePicker date, TextField time) {
        if (date.getValue() == null) {
            throw new IllegalArgumentException("Оберіть дату");
        }
        try {
            return LocalDateTime.of(date.getValue(), LocalTime.parse(time.getText().trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Час вводиться у форматі ГГ:ХХ, наприклад 09:30");
        }
    }

    public static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
