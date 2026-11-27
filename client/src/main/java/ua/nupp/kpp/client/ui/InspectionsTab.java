package ua.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.model.Inspection;
import ua.nupp.kpp.client.model.InspectionResult;
import ua.nupp.kpp.client.model.Sample;

import java.time.LocalDate;
import java.time.LocalTime;

/** Вкладка «Перевірки»: таблиця та CRUD перевірок проб. */
public class InspectionsTab extends BaseTab {
    private final ApiClient api;
    private final TableView<Inspection> table = new TableView<>();
    private final ComboBox<Sample> sampleBox = new ComboBox<>();
    private final TextField inspector = new TextField();
    private final DatePicker date = new DatePicker();
    private final TextField time = new TextField();
    private final ComboBox<InspectionResult> resultBox = new ComboBox<>();
    private final TextField note = new TextField();

    public InspectionsTab(ApiClient api) {
        super("Перевірки");
        this.api = api;

        table.getColumns().add(Ui.<Inspection>col("ID", i -> i.getId(), 50));
        table.getColumns().add(Ui.<Inspection>col("Проба №", i -> i.getSampleId(), 80));
        table.getColumns().add(Ui.<Inspection>col("Перевіряльник", i -> i.getInspector(), 180));
        table.getColumns().add(Ui.<Inspection>col("Дата", i -> i.getCheckedAt().format(Ui.DATE_TIME), 140));
        table.getColumns().add(Ui.<Inspection>col("Результат", i -> i.getResult(), 110));
        table.getColumns().add(Ui.<Inspection>col("Примітка", i -> i.getNote(), 300));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, i) -> {
            if (i != null) {
                fill(i);
            }
        });

        resultBox.setItems(FXCollections.observableArrayList(InspectionResult.values()));
        time.setPromptText("ГГ:ХХ");

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Проба", sampleBox);
        Ui.addRow(form, 1, "Перевіряльник", inspector);
        Ui.addRow(form, 2, "Дата", date);
        Ui.addRow(form, 3, "Час (ГГ:ХХ)", time);
        Ui.addRow(form, 4, "Результат", resultBox);
        Ui.addRow(form, 5, "Примітка", note);

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

        VBox root = new VBox(10, table, form, new HBox(8, add, save, delete, clear, reload));
        root.setPadding(new Insets(10));
        setContent(root);
        clearForm();
    }

    @Override
    public void refresh() {
        Ui.safe(() -> {
            sampleBox.setItems(FXCollections.observableArrayList(api.samples()));
            table.setItems(FXCollections.observableArrayList(api.inspections()));
        });
    }

    private Inspection fromForm() {
        Sample sample = sampleBox.getValue();
        if (sample == null) {
            throw new IllegalArgumentException("Оберіть пробу");
        }
        Inspection i = new Inspection();
        i.setSampleId(sample.getId());
        i.setInspector(Ui.text(inspector));
        i.setCheckedAt(Ui.dateTime(date, time));
        i.setResult(resultBox.getValue());
        i.setNote(Ui.text(note));
        return i;
    }

    private void add() {
        api.createInspection(fromForm());
        clearForm();
        refresh();
    }

    private void save() {
        Inspection selected = selected();
        Inspection i = fromForm();
        i.setId(selected.getId());
        api.updateInspection(i);
        refresh();
    }

    private void delete() {
        Inspection selected = selected();
        if (Ui.confirm("Видалити перевірку №" + selected.getId() + "?")) {
            api.deleteInspection(selected.getId());
            clearForm();
            refresh();
        }
    }

    private Inspection selected() {
        Inspection i = table.getSelectionModel().getSelectedItem();
        if (i == null) {
            throw new IllegalArgumentException("Спочатку оберіть перевірку в таблиці");
        }
        return i;
    }

    private void fill(Inspection i) {
        for (Sample s : sampleBox.getItems()) {
            if (s.getId() == i.getSampleId()) {
                sampleBox.setValue(s);
            }
        }
        inspector.setText(i.getInspector());
        date.setValue(i.getCheckedAt().toLocalDate());
        time.setText(i.getCheckedAt().toLocalTime().withSecond(0).withNano(0).toString());
        resultBox.setValue(i.getResult());
        note.setText(i.getNote() == null ? "" : i.getNote());
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        sampleBox.setValue(null);
        inspector.clear();
        date.setValue(LocalDate.now());
        time.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        resultBox.setValue(null);
        note.clear();
    }
}
