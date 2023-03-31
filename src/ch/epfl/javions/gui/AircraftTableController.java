package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
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
import java.util.function.Function;

public final class AircraftTableController {
    private final TableView<ObservableAircraftState> tableView;
    private final Pane pane;
    private Consumer<ObservableAircraftState> doubleClickConsumer;

    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
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
                newStringColumn("OACI",
                        s -> makeObservable(s.address()).map(IcaoAddress::string)),
                newStringColumn("Indicatif",
                        s -> s.callSignProperty().map(CallSign::string)),
                newStringColumn("Immatriculation",
                        s -> makeObservable(s.aircraftData()).map(d -> d.registration().string())),
                newStringColumn("Modèle",
                        s -> makeObservable(s.aircraftData()).map(AircraftData::model)),
                newDoubleColumn("Longitude (°)",
                        s -> convert(s.positionProperty().map(GeoPos::longitude)),
                        Units.Angle.DEGREE,
                        4),
                newDoubleColumn("Latitude (°)",
                        s -> convert(s.positionProperty().map(GeoPos::latitude)),
                        Units.Angle.DEGREE,
                        4),
                newDoubleColumn("Altitude (m)",
                        ObservableAircraftState::altitudeProperty,
                        Units.Length.METER,
                        0),
                newDoubleColumn("Vitesse (km/h)",
                        ObservableAircraftState::speedProperty,
                        Units.Speed.KILOMETER_PER_HOUR,
                        0));

        var tableView = new TableView<ObservableAircraftState>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setTableMenuButtonVisible(true);
        tableView.getColumns().setAll(columns);
        return tableView;
    }

    private static TableColumn<ObservableAircraftState, String> newStringColumn(
            String title,
            Function<ObservableAircraftState, ObservableValue<String>> propertyExtractor) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.setCellValueFactory(f -> propertyExtractor.apply(f.getValue()));
        return column;
    }

    private static TableColumn<ObservableAircraftState, String> newDoubleColumn(
            String title,
            Function<ObservableAircraftState, ObservableDoubleValue> propertyExtractor,
            double unit,
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
            return Bindings.when(Bindings.greaterThan(p, Double.NEGATIVE_INFINITY))
                    .then(formatter.format(Units.convertTo(p.get(), unit)))
                    .otherwise("");
        });

        // Change comparator to sort values numerically
        column.setComparator((s1, s2) -> s1.isEmpty() || s2.isEmpty()
                ? s2.compareTo(s1)
                : Double.compare(parseSafeDouble(formatter, s1), parseSafeDouble(formatter, s2)));

        return column;
    }

    private static <T> ObservableValue<T> makeObservable(T value) {
        return Bindings.createObjectBinding(() -> value);
    }

    private static ObservableDoubleValue convert(ObservableValue<Double> observableValue) {
        return Bindings.createDoubleBinding(observableValue::getValue, observableValue);
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
