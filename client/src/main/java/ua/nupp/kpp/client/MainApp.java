package ua.nupp.kpp.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ua.nupp.kpp.client.api.ApiClient;
import ua.nupp.kpp.client.ui.*;

/** Головне вікно: вкладки для пунктів, проб, вимірювань, перевірок та імпорту CSV. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        ApiClient api = new ApiClient();

        PointsTab points = new PointsTab(api);
        SamplesTab samples = new SamplesTab(api);
        MeasurementsTab measurements = new MeasurementsTab(api);
        InspectionsTab inspections = new InspectionsTab(api);
        ImportTab importTab = new ImportTab(api, measurements::refresh);

        TabPane tabs = new TabPane(points, samples, measurements, inspections, importTab);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab instanceof BaseTab) {
                ((BaseTab) newTab).refresh();
            }
        });

        stage.setTitle("Моніторинг якості води");
        stage.setScene(new Scene(tabs, 1100, 650));
        stage.show();
        points.refresh();
    }
}
