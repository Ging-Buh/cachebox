package CB_Locator.Map;

import CB_Locator.Map.Layer.LayerType;
import CB_Locator.Map.Layer.MapType;

public class DefaultLayerList extends java.util.ArrayList<Layer> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DefaultLayerList() {
        // now using Layer.FriendlyName for Translation
        this.add(new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, "Mapnik", "Mapnik", "http://c.tile.openstreetmap.org/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.normal, Layer.StorageType.PNG, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png"));
        // this.add(new Layer(MapType.ONLINE, Type.normal, "Esri", "", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"));
        // this.add(new Layer(MapType.ONLINE, Type.normal, "Google Hybrid", "", "http://mt0.google.com/vt/lyrs=y@142&x={x}&y={y}&z={z}"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "hillshading", "hillshading", "http://a.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "hiking", "hiking", "http://tile.waymarkedtrails.org/hillshading/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "public_transport", "public_transport", "http://tile.memomaps.de/tilegen/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "railway", "railway", "http://a.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png")); // Eisenbahn
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "cycling", "cycling", "http://a.www.toolserver.org/tiles/bicycle_network/{z}/{x}/{y}.png")); // Radwege Alternative
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "mtb", "mtb", "http://tile.waymarkedtrails.org/mtb/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "riding", "riding", "http://tile.waymarkedtrails.org/riding/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "skating", "skating", "http://tile.waymarkedtrails.org/skating/{z}/{x}/{y}.png"));
        this.add(new Layer(MapType.ONLINE, LayerType.overlay, Layer.StorageType.PNG, "slopemap", "slopemap", "http://tile.waymarkedtrails.org/slopemap/{z}/{x}/{y}.png"));
    }

}
