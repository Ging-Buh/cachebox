package org.mapsforge.map.swing.view;

import java.awt.event.MouseEvent;
import java.util.prefs.BackingStoreException;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.swing.controller.MouseEventListener;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;

public class GpsSimmulatorMouseEventListener extends MouseEventListener
{

	public GpsSimmulatorMouseEventListener(Model model)
	{
		super(model);
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent)
	{
		super.mouseDragged(mouseEvent);
		// Save last Point
		LatLong pos = this.mapViewPosition.getCenter();
		int zoom = this.mapViewPosition.getZoomLevel();
		SimulatorMain.prefs.putInt("zoom", zoom);
		SimulatorMain.prefs.putDouble("lat", pos.getLatitude());
		SimulatorMain.prefs.putDouble("lon", pos.getLongitude());
		try
		{
			SimulatorMain.prefs.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}

		GPSData.setLatitude(pos.getLatitude());
		GPSData.setLongitude(pos.getLongitude());

	}

}
