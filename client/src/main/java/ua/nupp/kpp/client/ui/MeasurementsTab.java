package ua.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.model.Measurement;
import ua.nupp.kpp.client.model.Sample;

import java.util.List;

/** Вкладка «Вимірювання»: таблиця з фільтром за пробою та CRUD. */
public class MeasurementsTab extends BaseTab {
    private final ApiClient api;
    private final TableView<Measurement> table = new TableView<>();
    private final ComboBox<Sample> filterBox = new ComboBox<>();
    private final ComboBox<Sample> sampleBox = new ComboBox<>();
    private final TextField parameter = new TextField();
    private final TextField value = new TextField();
    private final TextField unit = new TextField();
    private final TextField normLimit = new TextField();
    private boolean updatingFilter;

    public MeasurementsTab(ApiClient api) {
        super("Вимірювання");
        this.api = api;

        table.getColumns().add(Ui.<Measurement>col("ID", m -> m.getId(), 50));
        table.getColumns().add(Ui.<Measurement>col("Проба №", m -> m.getSampleId(), 80));
        table.getColumns().add(Ui.<Measurement>col("Параметр", m -> m.getParameter(), 200));
        table.getColumns().add(Ui.<Measurement>col("Значення", m -> m.getValue(), 110));
        table.getColumns().add(Ui.<Measurement>col("Одиниця", m -> m.getUnit(), 100));
        table.getColumns().add(Ui.<Measurement>col("Норматив", m -> m.getNormLimit(), 110));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, m) -> {
            if (m != null) {
                fill(m);
            }
        });

        filterBox.valueProperty().addListener((obs, old, s) -> {
            if (!updatingFilter) {
                loadTable();
            }
        });
        Button showAll = new Button("Показати всі");
        showAll.setOnAction(e -> {
            filterBox.setValue(null);
            loadTable();
        });

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Проба", sampleBox);
        Ui.addRow(form, 1, "Параметр", parameter);
        Ui.addRow(form, 2, "Значення", value);
        Ui.addRow(form, 3, "Одиниця", unit);
        Ui.addRow(form, 4, "Норматив", normLimit);

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

        VBox root = new VBox(10, new HBox(8, new Label("Фільтр за пробою:"), filterBox, showAll),
                table, form, new HBox(8, add, save, delete, clear, reload));
        root.setPadding(new Insets(10));
        setContent(root);
    }

    @Override
    public void refresh() {
        Ui.safe(() -> {
            Sample current = filterBox.getValue();
            List<Sample> samples = api.samples();
            updatingFilter = true;
            filterBox.setItems(FXCollections.observableArrayList(samples));
            sampleBox.setItems(FXCollections.observableArrayList(samples));
            if (current != null) {
                for (Sample s : samples) {
                    if (s.getId() == current.getId()) {
                        filterBox.setValue(s);
                    }
                }
            }
            updatingFilter = false;
            loadTable();
        });
    }

    private void loadTable() {
        Ui.safe(() -> {
            Sample filter = filterBox.getValue();
            table.setItems(FXCollections.observableArrayList(api.measurements(filter == null ? null : filter.getId())));
        });
    }

    private Measurement fromForm() {
        Sample sample = sampleBox.getValue();
        if (sample == null) {
            throw new IllegalArgumentException("Оберіть пробу");
        }
        Measurement m = new Measurement();
        m.setSampleId(sample.getId());
        m.setParameter(Ui.text(parameter));
        m.setValue(Ui.decimal(value, "значення"));
        m.setUnit(Ui.text(unit));
        m.setNormLimit(Ui.decimal(normLimit, "норматив"));
        return m;
    }

    private void add() {
        api.createMeasurement(fromForm());
        clearForm();
        loadTable();
    }

    private void save() {
        Measurement selected = selected();
        Measurement m = fromForm();
        m.setId(selected.getId());
        api.updateMeasurement(m);
        loadTable();
    }

    private void delete() {
        Measurement selected = selected();
        if (Ui.confirm("Видалити вимірювання «" + selected.getParameter() + "»?")) {
            api.deleteMeasurement(selected.getId());
            clearForm();
            loadTable();
        }
    }

    private Measurement selected() {
        Measurement m = table.getSelectionModel().getSelectedItem();
        if (m == null) {
            throw new IllegalArgumentException("Спочатку оберіть вимірювання в таблиці");
        }
        return m;
    }

    private void fill(Measurement m) {
        for (Sample s : sampleBox.getItems()) {
            if (s.getId() == m.getSampleId()) {
                sampleBox.setValue(s);
            }
        }
        parameter.setText(m.getParameter());
        value.setText(m.getValue().toPlainString());
        unit.setText(m.getUnit());
        normLimit.setText(m.getNormLimit().toPlainString());
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        sampleBox.setValue(null);
        parameter.clear();
        value.clear();
        unit.clear();
        normLimit.clear();
    }
}
