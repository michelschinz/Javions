package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class PlaneTableManager {
    private final TableView<ObservablePlaneState> tableView;
    private final Pane pane;

    public PlaneTableManager(ObservableSet<ObservablePlaneState> planes,
                             ObjectProperty<ObservablePlaneState> selectedAddressProperty) {
        var tableView = createTableView();
        var pane = new BorderPane(tableView);
        pane.getStylesheets().add("table.css");

        this.tableView = tableView;
        this.pane = pane;

        installListeners(planes);
        selectedAddressProperty.addListener((p, o, n) -> {
            tableView.getSelectionModel().select(n);
            tableView.scrollTo(n);
        });
    }

    private static TableView<ObservablePlaneState> createTableView() {
        var tableView = new TableView<ObservablePlaneState>();

        var callSignColumn = new TableColumn<ObservablePlaneState, String>("Vol");
        callSignColumn.setCellValueFactory(new PropertyValueFactory<>("callSign"));

        var altColumn = newNumericColumn(
                "Alt. (m)",
                ObservablePlaneState::altitudeProperty,
                DoubleUnaryOperator.identity(),
                0);
        var speedColumn = newNumericColumn(
                "Vit. (km/h)",
                ObservablePlaneState::velocityProperty,
                Units.converter(Units.Speed.KILOMETERS_PER_HOUR),
                0);

        tableView.getColumns().setAll(List.of(callSignColumn, altColumn, speedColumn));
        return tableView;
    }

    private static TableColumn<ObservablePlaneState, String> newNumericColumn(
            String title,
            Function<ObservablePlaneState, DoubleProperty> propertyExtractor,
            DoubleUnaryOperator valueTransformer,
            int fractionDigits) {
        var column = new TableColumn<ObservablePlaneState, String>(title);

        // Change cell factory to add "numeric" style class to the cells.
        var originalCellFactory = column.getCellFactory();
        column.setCellFactory(c -> {
            var cell = originalCellFactory.call(c);
            cell.getStyleClass().add("numeric");
            return cell;
        });

        // Change value factory to print NaNs as empty strings
        var formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(fractionDigits);

        column.setCellValueFactory(f -> {
            var p = propertyExtractor.apply(f.getValue());
            return Bindings.when(p.greaterThan(Double.NEGATIVE_INFINITY))
                    .then(formatter.format(valueTransformer.applyAsDouble(p.get())))
                    .otherwise("");
        });

        // Change comparator to sort values numerically
        column.setComparator((s1, s2) -> s1.isEmpty() || s2.isEmpty()
                ? s2.compareTo(s1)
                : Double.compare(parseSafeDouble(formatter, s1), parseSafeDouble(formatter, s2)));

        return column;
    }

    private static double parseSafeDouble(NumberFormat format, String string) {
        try {
            return format.parse(string).doubleValue();
        } catch (ParseException e) {
            throw new Error(e);
        }
    }

    private void installListeners(ObservableSet<ObservablePlaneState> planes) {
        planes.addListener((SetChangeListener<ObservablePlaneState>) c -> {
            var tableItems = tableView.getItems();
            if (c.wasRemoved()) tableItems.remove(c.getElementRemoved());
            if (c.wasAdded()) tableItems.add(c.getElementAdded());
        });
    }

    public Node pane() {
        return pane;
    }
}
