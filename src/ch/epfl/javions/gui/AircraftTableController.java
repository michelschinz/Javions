package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AircraftTableController {
    private final TableView<ObservableAircraftState> tableView;
    private Consumer<ObservableAircraftState> doubleClickConsumer;

    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAddressProperty) {
        var tableView = createTableView();
        tableView.getStylesheets().add("table.css");

        this.tableView = tableView;

        installHandlers();
        installListeners(aircraftStates, selectedAddressProperty);
    }

    private static TableView<ObservableAircraftState> createTableView() {
        var tableView = new TableView<ObservableAircraftState>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        //noinspection unchecked
        tableView.getColumns().setAll(
                newTextColumn("OACI", 60,
                        s -> constantObservable(s.address().string())),
                newTextColumn("Indicatif", 70,
                        s -> s.callSignProperty().map(CallSign::string)),
                newTextColumn("Immatriculation", 90,
                        s -> constantObservable(s.aircraftData()).map(d -> d.registration().string())),
                newTextColumn("Modèle", 230,
                        s -> constantObservable(s.aircraftData()).map(AircraftData::model)),
                newTextColumn("Type", 50,
                        s -> constantObservable(s.aircraftData()).map(d -> d.typeDesignator().string())),
                newTextColumn("Description", 70,
                        s -> constantObservable(s.aircraftData()).map(d -> d.description().string())),
                newDoubleColumn("Longitude (°)",
                        s -> asDoubleExpression(s.positionProperty().map(GeoPos::longitude)),
                        Units.Angle.DEGREE,
                        4),
                newDoubleColumn("Latitude (°)",
                        s -> asDoubleExpression(s.positionProperty().map(GeoPos::latitude)),
                        Units.Angle.DEGREE,
                        4),
                newDoubleColumn("Altitude (m)",
                        ObservableAircraftState::altitudeProperty,
                        Units.Length.METER,
                        0),
                newDoubleColumn("Vitesse (km/h)",
                        ObservableAircraftState::velocityProperty,
                        Units.Speed.KILOMETER_PER_HOUR,
                        0));
        return tableView;
    }

    private static TableColumn<ObservableAircraftState, String> newTextColumn(
            String title,
            int prefWidth,
            Function<ObservableAircraftState, ObservableValue<String>> propertyExtractor) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.setCellValueFactory(f -> propertyExtractor.apply(f.getValue()));
        column.setPrefWidth(prefWidth);
        return column;
    }

    private static TableColumn<ObservableAircraftState, String> newDoubleColumn(
            String title,
            Function<ObservableAircraftState, DoubleExpression> propertyExtractor,
            double unit,
            int fractionDigits) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.setPrefWidth(85);

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
                    .then(formatter.format(Units.convertTo(p.get(), unit)))
                    .otherwise("");
        });

        // Change comparator to sort values numerically
        column.setComparator((s1, s2) -> s1.isEmpty() || s2.isEmpty()
                ? s2.compareTo(s1)
                : Double.compare(parseSafeDouble(formatter, s1), parseSafeDouble(formatter, s2)));

        return column;
    }

    private static <T> ObservableValue<T> constantObservable(T value) {
        return new ReadOnlyObjectWrapper<>(value);
    }

    private static DoubleExpression asDoubleExpression(ObservableValue<Double> observableValue) {
        return Bindings.createDoubleBinding(observableValue::getValue, observableValue);
    }

    private static double parseSafeDouble(NumberFormat format, String string) {
        try {
            return format.parse(string).doubleValue();
        } catch (ParseException e) {
            throw new Error(e);
        }
    }

    private void installHandlers() {
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2
                && MouseButton.PRIMARY.equals(e.getButton())
                && doubleClickConsumer != null)
                doubleClickConsumer.accept(tableView.getSelectionModel().getSelectedItem());
        });
    }

    private void installListeners(ObservableSet<ObservableAircraftState> aircraftStates,
                                  ObjectProperty<ObservableAircraftState> selectedAddressProperty) {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasRemoved()) tableView.getItems().remove(c.getElementRemoved());
            if (c.wasAdded()) tableView.getItems().add(c.getElementAdded());
        });

        selectedAddressProperty.addListener((p, o, n) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), n))
                tableView.scrollTo(n);
            tableView.getSelectionModel().select(n);
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> selectedAddressProperty.set(n));
    }

    public Node pane() {
        return tableView;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> callback) {
        doubleClickConsumer = callback;
    }
}
