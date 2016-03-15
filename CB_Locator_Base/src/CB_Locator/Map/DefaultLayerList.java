package CB_Locator.Map;

import CB_Locator.Map.Layer.Type;

public class DefaultLayerList extends java.util.ArrayList<Layer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultLayerList() {
		// now using Layer.FriendlyName for Translation		
		// !!! Layer.Name (HillShade) is "misused" for final url-generation
		this.add(new Layer(Type.normal, "Mapnik", "Mapnik", "http://c.tile.openstreetmap.org/"));
		this.add(new Layer(Type.normal, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/"));
		//this.add(new Layer(Type.overlay, "HillShade", "HillShade", "http://129.206.74.245:8004/tms_hs.ashx")); // http://openmapsurfer.uni-hd.de/ ist auch tot
		// this.add(new Layer(Type.overlay, "HillShade", "HillShade", "http://openmapsurfer.uni-hd.de:8004/tms_hs.ashx")); // siehe vorige Zeile
		// this.add(new Layer(Type.overlay, "Hill-Shade2", "Hill-Shade2", "http://toolserver.org/~cmarqu/hill/")); // 1. Juli 2014 abgeschaltet 
		// this.add(new Layer(Type.overlay, "Wanderwege", "Wanderwege", "http://tile.lonvia.de/hiking/")); // umgezogen nach http://tile.waymarkedtrails.org
		this.add(new Layer(Type.overlay, "hillshading", "hillshading", "http://a.tiles.wmflabs.org/hillshading/"));
		this.add(new Layer(Type.overlay, "hiking", "hiking", "http://tile.waymarkedtrails.org/hillshading/"));
		// this.add(new Layer(Type.overlay, "cycling", "cycling", "http://tile.waymarkedtrails.org/cycling/"));
		this.add(new Layer(Type.overlay, "public_transport", "public_transport", "http://tile.memomaps.de/tilegen/"));
		this.add(new Layer(Type.overlay, "railway", "railway", "http://a.tiles.openrailwaymap.org/standard/")); // Eisenbahn
		this.add(new Layer(Type.overlay, "cycling", "cycling", "http://a.www.toolserver.org/tiles/bicycle_network/")); // Radwege Alternative
		this.add(new Layer(Type.overlay, "mtb", "mtb", "http://tile.waymarkedtrails.org/mtb/"));
		this.add(new Layer(Type.overlay, "riding", "riding", "http://tile.waymarkedtrails.org/riding/"));
		this.add(new Layer(Type.overlay, "skating", "skating", "http://tile.waymarkedtrails.org/skating/"));
		this.add(new Layer(Type.overlay, "slopemap", "slopemap", "http://tile.waymarkedtrails.org/slopemap/"));
	}

}
