/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.swing.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.prefs.BackingStoreException;

import javax.swing.SwingUtilities;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.Model;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;

public class MouseEventListener implements MouseListener, MouseMotionListener, MouseWheelListener
{
	private Point lastDragPoint;
	private final MapViewPosition mapViewPosition;

	public MouseEventListener(Model model)
	{
		this.mapViewPosition = model.mapViewPosition;
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent)
	{
		// do nothing
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent)
	{
		if (SwingUtilities.isLeftMouseButton(mouseEvent))
		{
			Point point = mouseEvent.getPoint();
			if (this.lastDragPoint != null)
			{
				int moveHorizontal = point.x - this.lastDragPoint.x;
				int moveVertical = point.y - this.lastDragPoint.y;
				this.mapViewPosition.moveCenter(moveHorizontal, moveVertical);

				// Save last Point
				LatLong pos = this.mapViewPosition.getCenter();
				int zoom = this.mapViewPosition.getZoomLevel();
				SimulatorMain.prefs.putInt("zoom", zoom);
				SimulatorMain.prefs.putDouble("lat", pos.latitude);
				SimulatorMain.prefs.putDouble("lon", pos.longitude);
				try
				{
					SimulatorMain.prefs.flush();
				}
				catch (BackingStoreException e)
				{
					e.printStackTrace();
				}

				GPSData.setLatitude(pos.latitude);
				GPSData.setLongitude(pos.longitude);
			}
			this.lastDragPoint = point;
		}
	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent)
	{
		// do nothing
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent)
	{
		// do nothing
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent)
	{
		// do nothing
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent)
	{
		if (SwingUtilities.isLeftMouseButton(mouseEvent))
		{
			this.lastDragPoint = mouseEvent.getPoint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent)
	{
		this.lastDragPoint = null;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
	{
		byte zoomLevelDiff = (byte) -mouseWheelEvent.getWheelRotation();
		this.mapViewPosition.zoom(zoomLevelDiff);
	}
}
