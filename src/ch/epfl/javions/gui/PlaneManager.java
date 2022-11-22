package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneState;
import ch.epfl.javions.PointWebMercator;
import ch.epfl.javions.WebMercator;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.HashSet;

public final class PlaneManager {
    private static final String AIRLINER = "M.01 14.75c-.26 0-.74-.71-.86-1.41l-3.33.86L-4.5 14.29l.08-1.41.11-.07c1.13-.68 2.68-1.64 3.2-2-.37-1.06-.51-3.92-.43-8.52v0L-4.5 2.31C-7.13 3.12-11.3 4.39-11.5 4.5a.5.5 0 01-.21 0 .52.52 0 01-.49-.45 1 1 0 01.52-1l1.74-.91c1.36-.71 3.22-1.69 4.66-2.43a4 4 0 010-.52c0-.69 0-1 0-1.14l.25-.13H-5.34A1.07 1.07 0 01-4.26-3.27 1.12 1.12 0 01-3.44-3a1.46 1.46 0 01.26.87L-3.42-2h.25c0 .14 0 .31 0 .58l1.52-.84c0-1.48 0-7.06 1.1-8.25a.74.74 0 011.13 0c1.15 1.19 1.13 6.78 1.1 8.25l1.52.84c0-.32 0-.48 0-.58l.25-.13H3.2A1.46 1.46 0 013.5-3a1.11 1.11 0 01.82-.28 1.06 1.06 0 011.08 1.16V-2c0 .19 0 .48 0 1.17a4 4 0 010 .52c1.75.9 4.4 2.29 5.67 3l.73.38a.9.9 0 01.5 1 .55.55 0 01-.5.47h0l-.11 0c-.28-.11-4.81-1.49-7.16-2.2H1.56v0c.09 4.6-.06 7.46-.43 8.52.52.33 2.07 1.29 3.2 2l.11.07L4.5 14.29l-.33-.09-3.33-.86c-.12.7-.6 1.41-.86 1.41h0Z";

    private final ObjectProperty<MapViewParameters> mapViewParametersProperty;
    private final ObservableMap<IcaoAddress, PlaneState> planeStates;
    private final Pane pane;

    public PlaneManager(ObjectProperty<MapViewParameters> mapViewParametersProperty,
                        ObservableMap<IcaoAddress, PlaneState> planeStates) {
        var pane = new Pane();
        pane.getStylesheets().add("planes.css");
        pane.setMouseTransparent(true);

        this.mapViewParametersProperty = mapViewParametersProperty;
        this.planeStates = planeStates;
        this.pane = pane;

        installHandlers();
    }

    private void installHandlers() {
        mapViewParametersProperty.addListener((p, o, n) -> layoutPlanes());
        planeStates.addListener((Observable o) -> {
            synchronizePlanes();
            layoutPlanes();
        });
    }

    public Pane pane() {
        return pane;
    }

    private void synchronizePlanes() {
        var missingAddresses = new HashSet<>(planeStates.keySet());

        // Remove obsolete nodes and compute missing ones.
        pane.getChildren().removeIf(n -> !missingAddresses.remove(IcaoAddress.of(n.getId())));

        // Add nodes for missing planes
        var newNodes = new ArrayList<Node>(missingAddresses.size());
        for (var icaoAddress : missingAddresses) {
            var planePath = new SVGPath();
            planePath.setContent(AIRLINER);
            planePath.getStyleClass().add("plane");
            planePath.setId(icaoAddress.toString());

            newNodes.add(planePath);
        }
        pane.getChildren().addAll(newNodes);
    }

    private void layoutPlanes() {
        var mapViewParameters = mapViewParametersProperty.get();
        for (var node : pane.getChildren()) {
            node.setVisible(false);

            var state = planeStates.get(IcaoAddress.of(node.getId()));
            if (state == null) continue;

            var pos = state.position();
            if (pos == null) continue;
            var pt = new PointWebMercator(WebMercator.x(pos.longitude()), WebMercator.y(pos.latitude()));
            // Note: we can't use relocate here, as it positions the plane's *top left* corner,
            //   not its actual origin (which is what we want).
            node.setLayoutX(mapViewParameters.viewX(pt));
            node.setLayoutY(mapViewParameters.viewY(pt));

            var heading = state.trackOrHeading();
            node.setRotate(Math.toDegrees(heading));

            node.setVisible(true);
        }
    }
}
