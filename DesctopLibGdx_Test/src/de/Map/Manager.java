package de.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;

import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;

public class Manager extends ManagerBase {
	
	public Manager()
	{
		// for the Access to the manager in the CB_Core
		CB_Core.Map.ManagerBase.Manager = this;
		// Layers.add(new Layer("MapsForge", "MapsForge", ""));
		Layers.add(new Layer("Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
		Layers.add(new Layer("OSM Cycle Map", "Open Cycle Map", "http://b.andy.sandbox.cloudmade.com/tiles/cycle/"));
		Layers.add(new Layer("TilesAtHome", "Osmarender", "http://a.tah.openstreetmap.org/Tiles/tile/"));
	}

}
