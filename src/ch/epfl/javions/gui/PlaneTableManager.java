package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.db.AircraftDatabase.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public final class PlaneTableManager {
    private final TableView<ObservablePlaneState> tableView;
    private final Pane pane;
    private Consumer<ObservablePlaneState> doubleClickConsumer;

    public PlaneTableManager(ObservableSet<ObservablePlaneState> planes,
                             ObjectProperty<ObservablePlaneState> selectedAddressProperty) {
        var tableView = createTableView();
        var pane = new BorderPane(tableView);
        pane.getStylesheets().add("table.css");

        this.tableView = tableView;
        this.pane = pane;

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2
                    && MouseButton.PRIMARY.equals(e.getButton())
                    && doubleClickConsumer != null)
                doubleClickConsumer.accept(tableView.getSelectionModel().getSelectedItem());
        });

        installListeners(planes);
        selectedAddressProperty.addListener((p, o, n) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), n))
                tableView.scrollTo(n);
            tableView.getSelectionModel().select(n);
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            selectedAddressProperty.set(n);
        });
    }

    private static TableView<ObservablePlaneState> createTableView() {
        var columns = List.of(
                newStringColumn("Vol", ObservablePlaneState::callSignProperty),
                newStringColumn("Enreg.", fixedDataExtractor(AircraftData::registration)),
                newStringColumn("Modèle", fixedDataExtractor(AircraftData::model)),
                newDoubleColumn(
                        "Lon. (°)",
                        lonLatExtractor(GeoPos::longitude),
                        Units.converter(Units.Angle.DEGREE),
                        4),
                newDoubleColumn(
                        "Lat. (°)",
                        lonLatExtractor(GeoPos::latitude),
                        Units.converter(Units.Angle.DEGREE),
                        4),
                newDoubleColumn(
                        "Alt. (m)",
                        ObservablePlaneState::altitudeProperty,
                        DoubleUnaryOperator.identity(),
                        0),
                newDoubleColumn(
                        "Vit. (km/h)",
                        ObservablePlaneState::velocityProperty,
                        Units.converter(Units.Speed.KILOMETERS_PER_HOUR),
                        0));

        var tableView = new TableView<ObservablePlaneState>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setTableMenuButtonVisible(true);
        tableView.getColumns().setAll(columns);
        return tableView;
    }

    private static Function<ObservablePlaneState, DoubleExpression> lonLatExtractor(ToDoubleFunction<GeoPos> function) {
        return state ->
                Bindings.createDoubleBinding(() -> {
                            var maybePosition = state.getPosition();
                            return maybePosition == null ? Double.NaN : function.applyAsDouble(maybePosition);
                        },
                        state.positionProperty());
    }

    private static Function<ObservablePlaneState, StringExpression> fixedDataExtractor(Function<AircraftData, String> f) {
        return state -> {
            var value = f.apply(state.getFixedData());
            return Bindings.createStringBinding(() -> value);
        };
    }

    private static TableColumn<ObservablePlaneState, String> newStringColumn(
            String title,
            Function<ObservablePlaneState, StringExpression> propertyExtractor) {
        var column = new TableColumn<ObservablePlaneState, String>(title);
        column.setCellValueFactory(f -> propertyExtractor.apply(f.getValue()));
        return column;
    }

    private static TableColumn<ObservablePlaneState, String> newDoubleColumn(
            String title,
            Function<ObservablePlaneState, DoubleExpression> propertyExtractor,
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
        formatter.setMinimumFractionDigits(fractionDigits);
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

    public void setOnDoubleClick(Consumer<ObservablePlaneState> callback) {
        doubleClickConsumer = callback;
    }
}
