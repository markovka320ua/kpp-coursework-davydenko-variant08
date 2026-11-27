package ua.nupp.kpp.client.ui;

import javafx.scene.control.Tab;

/** Спільний предок вкладок: кожна вміє перезавантажити свої дані з сервера. */
public abstract class BaseTab extends Tab {
    protected BaseTab(String title) {
        super(title);
        setClosable(false);
    }

    public abstract void refresh();
}
