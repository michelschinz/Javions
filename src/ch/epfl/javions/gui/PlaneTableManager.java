package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.Units;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.List;

public final class PlaneTableManager {
    private final TableView<ObservablePlaneState> tableView;
    private final Pane pane;

    public PlaneTableManager(ObservableMap<IcaoAddress, ObservablePlaneState> planes,
                             ObjectProperty<IcaoAddress> selectedAddressProperty) {
        var tableView = createTableView();
        var pane = new BorderPane(tableView);
        pane.getStylesheets().add("table.css");

        this.tableView = tableView;
        this.pane = pane;

        installListeners(planes);
        selectedAddressProperty.addListener((p, o, n) -> tableView.getSelectionModel().select(planes.get(n)));
    }

    private static TableView<ObservablePlaneState> createTableView() {
        var tableView = new TableView<ObservablePlaneState>();

        var callSignColumn = new TableColumn<ObservablePlaneState, String>("Vol");
        callSignColumn.setCellValueFactory(new PropertyValueFactory<>("callSign"));

        var altColumn = new TableColumn<ObservablePlaneState, String>("Alt. (m)");
        addStyleClassToCellsOf(altColumn, "altitude");
        altColumn.setCellValueFactory(f ->
                Bindings.when(f.getValue().altitudeProperty().greaterThan(Double.NEGATIVE_INFINITY))
                        .then(Integer.toString((int) f.getValue().getAltitude()))
                        .otherwise(""));
        altColumn.setComparator((s1, s2) -> s1.isBlank() || s2.isBlank()
                ? s1.compareTo(s2)
                : Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));

        var speedColumn = new TableColumn<ObservablePlaneState, String>("Vit. (km/h)");
        addStyleClassToCellsOf(speedColumn, "speed");
        speedColumn.setCellValueFactory(f ->
                Bindings.when(f.getValue().velocityProperty().greaterThan(Double.NEGATIVE_INFINITY))
                        .then(Integer.toString((int) (f.getValue().getVelocity() * (Units.Speed.METERS_PER_SECOND / Units.Speed.KILOMETERS_PER_HOUR))))
                        .otherwise(""));

        tableView.getColumns().setAll(List.of(callSignColumn, altColumn, speedColumn));
        return tableView;
    }

    private static <S, T> void addStyleClassToCellsOf(TableColumn<S, T> column, String styleClass) {
        var originalCellFactory = column.getCellFactory();
        column.setCellFactory(c -> {
            var cell = originalCellFactory.call(c);
            cell.getStyleClass().add(styleClass);
            return cell;
        });
    }

    private void installListeners(ObservableMap<IcaoAddress, ObservablePlaneState> planes) {
        planes.addListener((MapChangeListener<IcaoAddress, ObservablePlaneState>) c -> {
            var tableItems = tableView.getItems();
            if (c.wasRemoved())
                tableItems.remove(c.getValueRemoved());
            if (c.wasAdded())
                tableItems.add(c.getValueAdded());
        });
    }

    public Node pane() {
        return pane;
    }
}
