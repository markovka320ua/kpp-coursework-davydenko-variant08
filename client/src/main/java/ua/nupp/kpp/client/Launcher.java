package ua.nupp.kpp.client;

import javafx.application.Application;

/** Окремий клас із main(): дозволяє запускати JavaFX-програму з IDE без налаштування модулів. */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
