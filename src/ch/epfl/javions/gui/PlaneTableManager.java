package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public final class PlaneTableManager {
    private final TableView<ObservablePlaneState> tableView;

    public PlaneTableManager(ObservableMap<IcaoAddress, ObservablePlaneState> planes,
                             ObjectProperty<IcaoAddress> selectedAddressProperty) {
        var tableView = new TableView<ObservablePlaneState>();

        var callSignColumn = new TableColumn<ObservablePlaneState, String>("Flight");
        callSignColumn.setCellValueFactory(new PropertyValueFactory<>("callSign"));

        var altColumn = new TableColumn<ObservablePlaneState, String>("Alt.");
        altColumn.setCellValueFactory(f ->
                Bindings.when(f.getValue().altitudeProperty().greaterThan(Double.NEGATIVE_INFINITY))
                        .then(Integer.toString((int) f.getValue().getAltitude()))
                        .otherwise(""));
        altColumn.setCellFactory(col -> {
            var cell = (TableCell<ObservablePlaneState, String>) TableColumn.DEFAULT_CELL_FACTORY.call(col);
            cell.setAlignment(Pos.BASELINE_RIGHT);
            return cell;
        });
        altColumn.setComparator((s1, s2) -> s1.isBlank() || s2.isBlank()
                ? s1.compareTo(s2)
                : Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));

        var hdgColumn = new TableColumn<ObservablePlaneState, Double>("Hdg.");
        hdgColumn.setCellValueFactory(new PropertyValueFactory<>("trackOrHeading"));

        tableView.getColumns().setAll(List.of(callSignColumn, altColumn, hdgColumn));

        this.tableView = tableView;

        installListeners(planes);
        selectedAddressProperty.addListener((p, o, n) -> tableView.getSelectionModel().select(planes.get(n)));
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
        return tableView;
    }
}
