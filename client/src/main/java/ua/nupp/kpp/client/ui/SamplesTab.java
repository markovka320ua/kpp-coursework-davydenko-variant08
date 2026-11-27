package ua.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.model.IndexResult;
import ua.nupp.kpp.client.model.ObservationPoint;
import ua.nupp.kpp.client.model.Sample;
import ua.nupp.kpp.client.model.SampleState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вкладка «Проби»: CRUD, зміна стану життєвого циклу та показ інтегрального показника.
 * Допустимість переходів стану перевіряє сервер; клієнт лише надсилає запит і показує результат.
 */
public class SamplesTab extends BaseTab {
    private final ApiClient api;
    private final TableView<Sample> table = new TableView<>();
    private final Map<Integer, ObservationPoint> pointsById = new HashMap<>();
    private final ComboBox<ObservationPoint> pointBox = new ComboBox<>();
    private final DatePicker date = new DatePicker();
    private final TextField time = new TextField();
    private final TextField collector = new TextField();
    private final ComboBox<SampleState> stateBox = new ComboBox<>();
    private final Label indexLabel = new Label("Показник: оберіть пробу й натисніть «Обчислити показник»");

    public SamplesTab(ApiClient api) {
        super("Проби");
        this.api = api;

        table.getColumns().add(Ui.<Sample>col("ID", s -> s.getId(), 50));
        table.getColumns().add(Ui.<Sample>col("Пункт", s -> pointName(s.getPointId()), 260));
        table.getColumns().add(Ui.<Sample>col("Дата відбору", s -> s.getSampledAt().format(Ui.DATE_TIME), 140));
        table.getColumns().add(Ui.<Sample>col("Відбирач", s -> s.getCollector(), 160));
        table.getColumns().add(Ui.<Sample>col("Стан", s -> s.getState(), 130));
        table.getColumns().add(Ui.<Sample>col("Показник I", s -> s.getIntegralIndex(), 100));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                fill(s);
            }
        });

        stateBox.setItems(FXCollections.observableArrayList(SampleState.values()));
        time.setPromptText("ГГ:ХХ");

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Пункт", pointBox);
        Ui.addRow(form, 1, "Дата відбору", date);
        Ui.addRow(form, 2, "Час (ГГ:ХХ)", time);
        Ui.addRow(form, 3, "Відбирач", collector);

        Button add = new Button("Додати");
        add.setOnAction(e -> Ui.safe(this::add));
        Button save = new Button("Зберегти зміни");
        save.setOnAction(e -> Ui.safe(this::save));
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> Ui.safe(this::delete));
        Button clear = new Button("Очистити форму");
        clear.setOnAction(e -> clearForm());
        Button reload = new Button("Оновити");
        reload.setOnAction(e -> refresh());

        Button change = new Button("Змінити стан");
        change.setOnAction(e -> Ui.safe(this::changeState));
        Button calc = new Button("Обчислити показник");
        calc.setOnAction(e -> Ui.safe(this::showIndex));
        HBox lifecycle = new HBox(8, new Label("Новий стан:"), stateBox, change, calc);

        VBox root = new VBox(10, table, form, new HBox(8, add, save, delete, clear, reload), lifecycle, indexLabel);
        root.setPadding(new Insets(10));
        setContent(root);
        clearForm();
    }

    @Override
    public void refresh() {
        Ui.safe(() -> {
            List<ObservationPoint> points = api.points();
            pointsById.clear();
            for (ObservationPoint p : points) {
                pointsById.put(p.getId(), p);
            }
            pointBox.setItems(FXCollections.observableArrayList(points));
            table.setItems(FXCollections.observableArrayList(api.samples()));
        });
    }

    private String pointName(int pointId) {
        ObservationPoint p = pointsById.get(pointId);
        return p == null ? "№" + pointId : p.toString();
    }

    private Sample fromForm() {
        ObservationPoint point = pointBox.getValue();
        if (point == null) {
            throw new IllegalArgumentException("Оберіть пункт спостереження");
        }
        Sample s = new Sample();
        s.setPointId(point.getId());
        s.setSampledAt(Ui.dateTime(date, time));
        s.setCollector(Ui.text(collector));
        return s;
    }

    private void add() {
        api.createSample(fromForm());
        clearForm();
        refresh();
    }

    private void save() {
        Sample selected = selected();
        Sample s = fromForm();
        s.setId(selected.getId());
        api.updateSample(s);
        refresh();
    }

    private void delete() {
        Sample selected = selected();
        if (Ui.confirm("Видалити пробу №" + selected.getId() + "?")) {
            api.deleteSample(selected.getId());
            clearForm();
            refresh();
        }
    }

    private void changeState() {
        Sample selected = selected();
        SampleState target = stateBox.getValue();
        if (target == null) {
            throw new IllegalArgumentException("Оберіть новий стан проби");
        }
        api.changeState(selected.getId(), target);
        refresh();
        Ui.info("Пробу №" + selected.getId() + " переведено в стан «" + target + "»");
    }

    private void showIndex() {
        Sample selected = selected();
        IndexResult r = api.index(selected.getId());
        indexLabel.setText(String.format("Проба №%d:  F1 = %.2f,  F2 = %.2f,  F3 = %.2f,  I = %s",
                r.getSampleId(), r.getF1(), r.getF2(), r.getF3(), r.getIntegral()));
    }

    private Sample selected() {
        Sample s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            throw new IllegalArgumentException("Спочатку оберіть пробу в таблиці");
        }
        return s;
    }

    private void fill(Sample s) {
        ObservationPoint p = pointsById.get(s.getPointId());
        pointBox.setValue(p);
        date.setValue(s.getSampledAt().toLocalDate());
        time.setText(s.getSampledAt().toLocalTime().withSecond(0).withNano(0).toString());
        collector.setText(s.getCollector());
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        pointBox.setValue(null);
        date.setValue(LocalDate.now());
        time.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        collector.clear();
    }
}
