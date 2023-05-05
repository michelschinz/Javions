package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public final class StatusLineController {
    private final Pane statusPane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;

    public StatusLineController() {
        var aircraftCountProperty = new SimpleIntegerProperty();
        var messageCountProperty = new SimpleLongProperty();

        var leftStatusText = new Text();
        leftStatusText.textProperty().bind(Bindings.format("Aéronefs visibles : %d",
                aircraftCountProperty));
        var rightStatusText = new Text();
        rightStatusText.textProperty().bind(Bindings.format("Messages reçus : %,d",
                messageCountProperty));

        var statusPane = new BorderPane(null, null, rightStatusText, null, leftStatusText);
        statusPane.getStylesheets().add("status.css");

        this.statusPane = statusPane;
        this.aircraftCountProperty = aircraftCountProperty;
        this.messageCountProperty = messageCountProperty;
    }

    public Pane pane() {
        return statusPane;
    }

    public IntegerProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }
}
