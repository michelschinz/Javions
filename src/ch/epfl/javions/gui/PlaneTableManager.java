package ch.epfl.javions.gui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public final class PlaneTableManager {
    private final TableView<ObservablePlaneState> tableView;

    public PlaneTableManager(ObservableList<ObservablePlaneState> planes) {
        var tableView = new TableView<>(planes);

        var callSignColumn = new TableColumn<ObservablePlaneState, String>("Flight");
        callSignColumn.setCellValueFactory(new PropertyValueFactory<>("callSign"));

        var altColumn = new TableColumn<ObservablePlaneState, Double>("Alt.");
        altColumn.setCellValueFactory(new PropertyValueFactory<>("altitude"));

        var hdgColumn = new TableColumn<ObservablePlaneState, Double>("Hdg.");
        hdgColumn.setCellValueFactory(new PropertyValueFactory<>("trackOrHeading"));

        tableView.getColumns().setAll(List.of(callSignColumn, altColumn, hdgColumn));

        this.tableView = tableView;
    }

    public Node pane() {
        return tableView;
    }
}
