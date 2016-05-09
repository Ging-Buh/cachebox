package CB_Locator.Map;

import CB_Locator.Map.Layer.MapType;
import CB_Locator.Map.Layer.Type;

public class DefaultLayerList extends java.util.ArrayList<Layer> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DefaultLayerList() {
	// now using Layer.FriendlyName for Translation		
	// !!! Layer.Name (HillShade) is "misused" for final url-generation
	this.add(new Layer(MapType.ONLINE, Type.normal, "Mapnik", "Mapnik", "http://c.tile.openstreetmap.org/"));
	this.add(new Layer(MapType.ONLINE, Type.normal, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "hillshading", "hillshading", "http://a.tiles.wmflabs.org/hillshading/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "hiking", "hiking", "http://tile.waymarkedtrails.org/hillshading/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "public_transport", "public_transport", "http://tile.memomaps.de/tilegen/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "railway", "railway", "http://a.tiles.openrailwaymap.org/standard/")); // Eisenbahn
	this.add(new Layer(MapType.ONLINE, Type.overlay, "cycling", "cycling", "http://a.www.toolserver.org/tiles/bicycle_network/")); // Radwege Alternative
	this.add(new Layer(MapType.ONLINE, Type.overlay, "mtb", "mtb", "http://tile.waymarkedtrails.org/mtb/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "riding", "riding", "http://tile.waymarkedtrails.org/riding/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "skating", "skating", "http://tile.waymarkedtrails.org/skating/"));
	this.add(new Layer(MapType.ONLINE, Type.overlay, "slopemap", "slopemap", "http://tile.waymarkedtrails.org/slopemap/"));
    }

}
