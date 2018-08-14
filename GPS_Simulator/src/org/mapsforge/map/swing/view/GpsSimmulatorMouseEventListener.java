package org.mapsforge.map.swing.view;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.input.MouseEventListener;
import org.mapsforge.map.view.MapView;

import java.awt.event.MouseEvent;
import java.util.prefs.BackingStoreException;

public class GpsSimmulatorMouseEventListener extends MouseEventListener {

    public GpsSimmulatorMouseEventListener(MapView mapView) {
        super(mapView);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        // Save last Point

        AwtMapView mv = (AwtMapView) this.mapView;

        LatLong pos = mv.model.mapViewPosition.getCenter();
        int zoom = mv.model.mapViewPosition.getZoomLevel();
        SimulatorMain.prefs.putInt("zoom", zoom);
        SimulatorMain.prefs.putDouble("lat", pos.getLatitude());
        SimulatorMain.prefs.putDouble("lon", pos.getLongitude());
        try {
            SimulatorMain.prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        GPSData.setLatitude(pos.getLatitude());
        GPSData.setLongitude(pos.getLongitude());

    }

}
