package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public final class StatusLineController {
    private final Pane statusPane;
    private final IntegerProperty messageCountProperty;
    private final IntegerProperty aircraftCountProperty;

    public StatusLineController() {
        var messageCountProperty = new SimpleIntegerProperty();
        var aircraftCountProperty = new SimpleIntegerProperty();

        var leftStatusText = new Text();
        leftStatusText.textProperty().bind(Bindings.format("Aéronefs visibles : %d",
                aircraftCountProperty));
        var rightStatusText = new Text();
        rightStatusText.textProperty().bind(Bindings.format("Messages reçus : %,d",
                messageCountProperty));

        var statusPane = new BorderPane(null, null, rightStatusText, null, leftStatusText);
        statusPane.getStylesheets().add("status.css");

        this.statusPane = statusPane;
        this.messageCountProperty = messageCountProperty;
        this.aircraftCountProperty = aircraftCountProperty;
    }

    public Pane pane() {
        return statusPane;
    }

    public IntegerProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    public IntegerProperty messageCountProperty() {
        return messageCountProperty;
    }
}
