package ua.nupp.kpp.client.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.api.ApiException;
import ua.nupp.kpp.client.csv.MeasurementCsv;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Вкладка «Імпорт CSV». Файл читається й передається на сервер у фоновому потоці (JavaFX Task),
 * тому інтерфейс не блокується; хід виконання показує ProgressBar.
 * Повторний імпорт того самого файла створить дублікати вимірювань.
 */
public class ImportTab extends BaseTab {
    private final ApiClient api;
    private final Runnable onImported;
    private final TextField pathField = new TextField(Path.of("sample-data", "measurements.csv").toString());
    private final Button startButton = new Button("Запустити імпорт");
    private final ProgressBar progress = new ProgressBar(0);
    private final Label status = new Label("Готово до імпорту");
    private final TextArea log = new TextArea();
    private boolean running;

    public ImportTab(ApiClient api, Runnable onImported) {
        super("Імпорт CSV");
        this.api = api;
        this.onImported = onImported;

        Button browse = new Button("Огляд…");
        browse.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Оберіть CSV-файл");
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                pathField.setText(file.getPath());
            }
        });
        startButton.setOnAction(e -> startImport());

        progress.setMaxWidth(Double.MAX_VALUE);
        log.setEditable(false);
        VBox.setVgrow(log, Priority.ALWAYS);
        pathField.setPrefColumnCount(50);

        Label hint = new Label("Формат: UTF-8, заголовок " + MeasurementCsv.HEADER
                + ", десятковий роздільник — крапка. Повторний імпорт створює дублікати.");
        hint.setWrapText(true);

        VBox root = new VBox(10, hint, new HBox(8, new Label("Файл:"), pathField, browse),
                startButton, progress, status, log);
        root.setPadding(new Insets(10));
        setContent(root);
    }

    @Override
    public void refresh() {
        // дані на цій вкладці не залежать від сервера
    }

    private void log(String line) {
        Platform.runLater(() -> log.appendText(line + "\n"));
    }

    private void startImport() {
        if (running) {
            return;
        }
        Path path = Path.of(pathField.getText().trim());
        running = true;
        startButton.setDisable(true);
        log.clear();
        log("Початок імпорту: " + path);

        Task<int[]> task = new Task<int[]>() {
            @Override
            protected int[] call() throws Exception {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (lines.isEmpty()) {
                    throw new IllegalArgumentException("Файл порожній");
                }
                MeasurementCsv.checkHeader(lines.get(0));
                int total = lines.size() - 1;
                if (total <= 0) {
                    throw new IllegalArgumentException("У файлі немає рядків з даними");
                }
                int ok = 0;
                int skipped = 0;
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (!line.isBlank()) {
                        try {
                            api.createMeasurement(MeasurementCsv.parseLine(line));
                            ok++;
                            log("Рядок " + (i + 1) + ": імпортовано");
                        } catch (ApiException e) {
                            if (e.isConnectionProblem()) {
                                throw e;
                            }
                            skipped++;
                            log("Рядок " + (i + 1) + " пропущено (сервер): " + e.getMessage());
                        } catch (IllegalArgumentException e) {
                            skipped++;
                            log("Рядок " + (i + 1) + " пропущено: " + e.getMessage());
                        }
                    }
                    updateProgress(i, total);
                    updateMessage("Оброблено " + i + " з " + total);
                }
                return new int[]{ok, skipped};
            }
        };

        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            finish();
            int[] r = task.getValue();
            String summary = "Імпорт завершено. Успішно: " + r[0] + ", пропущено: " + r[1];
            status.setText(summary);
            log(summary);
            onImported.run();
            Ui.info(summary);
        });
        task.setOnFailed(e -> {
            finish();
            Throwable ex = task.getException();
            String msg;
            if (ex instanceof NoSuchFileException) {
                msg = "Файл не знайдено: " + path;
            } else if (ex instanceof java.nio.charset.MalformedInputException) {
                msg = "Файл не в кодуванні UTF-8";
            } else {
                msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            }
            status.setText("Імпорт перервано");
            log("ПОМИЛКА: " + msg);
            Ui.error(msg);
        });

        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private void finish() {
        progress.progressProperty().unbind();
        status.textProperty().unbind();
        running = false;
        startButton.setDisable(false);
    }
}
