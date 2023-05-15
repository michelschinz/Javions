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
                                   ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        var tableView = createTableView();
        tableView.getStylesheets().add("table.css");

        this.tableView = tableView;

        installHandlers();
        installListeners(aircraftStates, selectedAircraftProperty);
    }

    private static TableView<ObservableAircraftState> createTableView() {
        var tableView = new TableView<ObservableAircraftState>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        //noinspection unchecked
        tableView.getColumns().setAll(
                textColumn("OACI", 60,
                        s -> new ReadOnlyObjectWrapper<>(s.address().string())),
                textColumn("Indicatif", 70,
                        s -> s.callSignProperty().map(CallSign::string)),
                textColumn("Immatriculation", 90,
                        s -> new ReadOnlyObjectWrapper<>(s.aircraftData()).map(d -> d.registration().string())),
                textColumn("Modèle", 230,
                        s -> new ReadOnlyObjectWrapper<>(s.aircraftData()).map(AircraftData::model)),
                textColumn("Type", 50,
                        s -> new ReadOnlyObjectWrapper<>(s.aircraftData()).map(d -> d.typeDesignator().string())),
                textColumn("Description", 70,
                        s -> new ReadOnlyObjectWrapper<>(s.aircraftData()).map(d -> d.description().string())),
                doubleColumn("Longitude (°)",
                        s -> s.positionProperty().map(GeoPos::longitude),
                        Units.Angle.DEGREE,
                        4),
                doubleColumn("Latitude (°)",
                        s -> s.positionProperty().map(GeoPos::latitude),
                        Units.Angle.DEGREE,
                        4),
                doubleColumn("Altitude (m)",
                        ObservableAircraftState::altitudeProperty,
                        Units.Length.METER,
                        0),
                doubleColumn("Vitesse (km/h)",
                        ObservableAircraftState::velocityProperty,
                        Units.Speed.KILOMETER_PER_HOUR,
                        0));
        return tableView;
    }

    private static TableColumn<ObservableAircraftState, String> textColumn(
            String title,
            int prefWidth,
            Function<ObservableAircraftState, ObservableValue<String>> propertyExtractor) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.setCellValueFactory(f -> propertyExtractor.apply(f.getValue()));
        column.setPrefWidth(prefWidth);
        return column;
    }

    private static TableColumn<ObservableAircraftState, String> doubleColumn(
            String title,
            Function<ObservableAircraftState, ObservableValue<Number>> propertyExtractor,
            double unit,
            int fractionDigits) {
        var column = new TableColumn<ObservableAircraftState, String>(title);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(85);

        // Change value factory to print NaNs as empty strings
        var formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(fractionDigits);
        formatter.setMaximumFractionDigits(fractionDigits);

        column.setCellValueFactory(f -> propertyExtractor
                .apply(f.getValue())
                .map(n -> Double.isNaN(n.doubleValue())
                        ? ""
                        : formatter.format(Units.convertTo(n.doubleValue(), unit))));

        // Change comparator to sort values numerically
        column.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) return s1.compareTo(s2);
            try {
                var d1 = formatter.parse(s1).doubleValue();
                var d2 = formatter.parse(s2).doubleValue();
                return Double.compare(d1, d2);
            } catch (ParseException e) { throw new Error(e); /* can't happen */ }
        });

        return column;
    }

    private void installHandlers() {
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2
                && MouseButton.PRIMARY.equals(e.getButton())
                && doubleClickConsumer != null
                && tableView.getSelectionModel().getSelectedItem() != null)
                doubleClickConsumer.accept(tableView.getSelectionModel().getSelectedItem());
        });
    }

    private void installListeners(ObservableSet<ObservableAircraftState> aircraftStates,
                                  ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasRemoved()) tableView.getItems().remove(c.getElementRemoved());
            if (c.wasAdded()) {
                tableView.getItems().add(c.getElementAdded());
                tableView.sort();
            }
        });

        selectedAircraftProperty.addListener((p, o, n) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), n))
                tableView.scrollTo(n);
            tableView.getSelectionModel().select(n);
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> selectedAircraftProperty.set(n));
    }

    public Node pane() {
        return tableView;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> callback) {
        doubleClickConsumer = callback;
    }
}
