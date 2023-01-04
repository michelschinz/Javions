package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.AircraftData;
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

public final class AircraftTableManager {
    private final TableView<ObservableAircraftState> tableView;
    private final Pane pane;
    private Consumer<ObservableAircraftState> doubleClickConsumer;

    public AircraftTableManager(ObservableSet<ObservableAircraftState> aircraftStates,
                                ObjectProperty<ObservableAircraftState> selectedAddressProperty) {
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

        installListeners(aircraftStates);
        selectedAddressProperty.addListener((p, o, n) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), n))
                tableView.scrollTo(n);
            tableView.getSelectionModel().select(n);
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> selectedAddressProperty.set(n));
    }

    private static TableView<ObservableAircraftState> createTableView() {
        var columns = List.of(
                newStringColumn("Hex", s -> Bindings.createStringBinding(() -> s.address().toString())),
                newStringColumn("Vol", ObservableAircraftState::callSignProperty),
                newStringColumn("Enregistrement", fixedDataExtractor(aircraftData -> aircraftData.registration().toString())),
                newStringColumn("Modèle", fixedDataExtractor(AircraftData::model)),
                newDoubleColumn(
                        "Longitude (°)",
                        lonLatExtractor(GeoPos::longitude),
                        Units.converter(Units.Angle.DEGREE),
                        4),
                newDoubleColumn(
                        "Latitude (°)",
                        lonLatExtractor(GeoPos::latitude),
                        Units.converter(Units.Angle.DEGREE),
                        4),
                newDoubleColumn(
                        "Altitude (m)",
                        ObservableAircraftState::altitudeProperty,
                        DoubleUnaryOperator.identity(),
                        0),
                newDoubleColumn(
                        "Vitesse (km/h)",
                        ObservableAircraftState::velocityProperty,
                        Units.converter(Units.Speed.KILOMETERS_PER_HOUR),
                        0));

        var tableView = new TableView<ObservableAircraftState>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setTableMenuButtonVisible(true);
        tableView.getColumns().setAll(columns);
        return tableView;
    }

    private static Function<ObservableAircraftState, DoubleExpression> lonLatExtractor(ToDoubleFunction<GeoPos> function) {
        return state ->
                Bindings.createDoubleBinding(() -> {
                            var maybePosition = state.getPosition();
                            return maybePosition == null ? Double.NaN : function.applyAsDouble(maybePosition);
                        },
                        state.positionProperty());
    }

    private static Function<ObservableAircraftState, StringExpression> fixedDataExtractor(Function<AircraftData, String> f) {
        return state -> {
            var value = f.apply(state.getFixedData());
            return Bindings.createStringBinding(() -> value);
        };
    }

    private static TableColumn<ObservableAircraftState, String> newStringColumn(
            String title,
            Function<ObservableAircraftState, StringExpression> propertyExtractor) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.setCellValueFactory(f -> propertyExtractor.apply(f.getValue()));
        return column;
    }

    private static TableColumn<ObservableAircraftState, String> newDoubleColumn(
            String title,
            Function<ObservableAircraftState, DoubleExpression> propertyExtractor,
            DoubleUnaryOperator valueTransformer,
            int fractionDigits) {
        var column = new TableColumn<ObservableAircraftState, String>(title);

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

    private void installListeners(ObservableSet<ObservableAircraftState> aircraftStates) {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            var tableItems = tableView.getItems();
            if (c.wasRemoved()) tableItems.remove(c.getElementRemoved());
            if (c.wasAdded()) tableItems.add(c.getElementAdded());
        });
    }

    public Node pane() {
        return pane;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> callback) {
        doubleClickConsumer = callback;
    }
}
