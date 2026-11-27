package ua.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.model.ObservationPoint;
import ua.nupp.kpp.client.model.WaterUse;

/** Вкладка «Пункти спостереження»: таблиця, додавання, редагування, видалення. */
public class PointsTab extends BaseTab {
    private final ApiClient api;
    private final TableView<ObservationPoint> table = new TableView<>();
    private final TextField code = new TextField();
    private final TextField name = new TextField();
    private final TextField waterBody = new TextField();
    private final ComboBox<WaterUse> waterUse = new ComboBox<>();
    private final TextField sensitivity = new TextField();

    public PointsTab(ApiClient api) {
        super("Пункти спостереження");
        this.api = api;

        table.getColumns().add(Ui.<ObservationPoint>col("ID", p -> p.getId(), 50));
        table.getColumns().add(Ui.<ObservationPoint>col("Код", p -> p.getCode(), 90));
        table.getColumns().add(Ui.<ObservationPoint>col("Назва", p -> p.getName(), 280));
        table.getColumns().add(Ui.<ObservationPoint>col("Водойма", p -> p.getWaterBody(), 180));
        table.getColumns().add(Ui.<ObservationPoint>col("Водокористування", p -> p.getWaterUse(), 170));
        table.getColumns().add(Ui.<ObservationPoint>col("Чутливість (F3)", p -> p.getSensitivity(), 120));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, p) -> {
            if (p != null) {
                fill(p);
            }
        });

        waterUse.setItems(FXCollections.observableArrayList(WaterUse.values()));
        sensitivity.setPromptText("0–100");

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Код", code);
        Ui.addRow(form, 1, "Назва", name);
        Ui.addRow(form, 2, "Водойма", waterBody);
        Ui.addRow(form, 3, "Водокористування", waterUse);
        Ui.addRow(form, 4, "Чутливість 0–100", sensitivity);

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
    }

    @Override
    public void refresh() {
        Ui.safe(() -> table.setItems(FXCollections.observableArrayList(api.points())));
    }

    private ObservationPoint fromForm() {
        ObservationPoint p = new ObservationPoint();
        p.setCode(Ui.text(code));
        p.setName(Ui.text(name));
        p.setWaterBody(Ui.text(waterBody));
        p.setWaterUse(waterUse.getValue());
        p.setSensitivity(Ui.integer(sensitivity, "чутливість"));
        return p;
    }

    private void add() {
        api.createPoint(fromForm());
        clearForm();
        refresh();
    }

    private void save() {
        ObservationPoint selected = selected();
        ObservationPoint p = fromForm();
        p.setId(selected.getId());
        api.updatePoint(p);
        refresh();
    }

    private void delete() {
        ObservationPoint selected = selected();
        if (Ui.confirm("Видалити пункт «" + selected.getName() + "»?")) {
            api.deletePoint(selected.getId());
            clearForm();
            refresh();
        }
    }

    private ObservationPoint selected() {
        ObservationPoint p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            throw new IllegalArgumentException("Спочатку оберіть пункт у таблиці");
        }
        return p;
    }

    private void fill(ObservationPoint p) {
        code.setText(p.getCode());
        name.setText(p.getName());
        waterBody.setText(p.getWaterBody());
        waterUse.setValue(p.getWaterUse());
        sensitivity.setText(String.valueOf(p.getSensitivity()));
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        code.clear();
        name.clear();
        waterBody.clear();
        waterUse.setValue(null);
        sensitivity.clear();
    }
}
