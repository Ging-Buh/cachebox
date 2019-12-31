package org.mapsforge.map.swing.view;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.PlatformUIBase.IgetFileReturnListener;
import de.droidcachebox.locator.Location;
import de.droidcachebox.locator.Location.ProviderType;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.CB_InternalRenderTheme;
import de.droidcachebox.utils.FileFactory;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.input.MapViewComponentListener;
import org.mapsforge.map.awt.util.JavaPreferences;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MapPanel extends JPanel implements ActionListener {

    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private static final long serialVersionUID = 6067211877479396433L;

    private static AwtMapView mapView;

    public MapPanel() {
        mapView = createMapView();
        String MapPath = SimulatorMain.prefs.get("loadedMap", "");
        try {
            addLayers(mapView, MapPath);
        } catch (Exception e) {

            e.printStackTrace();
        }
        PreferencesFacade preferencesFacade = new JavaPreferences(Preferences.userNodeForPackage(MapView.class));
        final Model model = mapView.getModel();
        model.init(preferencesFacade);

        this.setPreferredSize(new Dimension(610, 610));
        this.setBorder(BorderFactory.createTitledBorder("Map"));
        mapView.setPreferredSize(new Dimension(600, 600));
        this.add(mapView);
        mapView.setVisible(true);
        Button pushButton2 = new Button("Load Map");
        add(pushButton2);
        pushButton2.addActionListener(this); // listen for Button press

        LatLong pos = new LatLong(SimulatorMain.prefs.getDouble("lat", 0), SimulatorMain.prefs.getDouble("lon", 0));
        GPSData.setLatitude(pos.getLatitude());
        GPSData.setLongitude(pos.getLongitude());
        model.mapViewPosition.setCenter(pos);
        model.mapViewPosition.setZoomLevel((byte) SimulatorMain.prefs.getInt("zoom", 16));

        GPSData.addChangeListener(new GPSDataListener() {

            @Override
            public void valueChanged() {
                LatLong pos = new LatLong(GPSData.getLatitude(), GPSData.getLongitude());
                model.mapViewPosition.setCenter(pos);
                Locator.getInstance().setNewLocation(new Location(pos.getLatitude(), pos.getLongitude(), GPSData.getQuality(), true, (float) GPSData.getSpeed(), true, GPSData.getCourse(), GPSData.getAltitude(), ProviderType.GPS));
            }
        });
    }

    private static void addLayers(MapView mapView, String MapPath) {
        LayerManager layerManager = mapView.getLayerManager();
        Layers layers = layerManager.getLayers();
        TileCache tileCache = createTileCache();
        layers.clear();
        layers.add(createTileRendererLayer(tileCache, mapView.getModel().mapViewPosition, layerManager, MapPath));
    }

    private static Layer createTileRendererLayer(TileCache tileCache, IMapViewPosition mapViewPosition, LayerManager layerManager, String MapPath) {
        java.io.File mapFile = new java.io.File(FileFactory.createFile(MapPath).getAbsolutePath());
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, new MapFile(mapFile), mapViewPosition, false, false, true, GRAPHIC_FACTORY);
        tileRendererLayer.setXmlRenderTheme(CB_InternalRenderTheme.OSMARENDER);
        return tileRendererLayer;
    }

    private static AwtMapView createMapView() {
        AwtMapView mapView = new AwtMapView();
        mapView.getFpsCounter().setVisible(true);
        // mapView.addComponentListener(new MapViewComponentListener(mapView,new Dimension(600, 600)));
        mapView.addComponentListener(new MapViewComponentListener(mapView));

        GpsSimmulatorMouseEventListener mouseEventListener = new GpsSimmulatorMouseEventListener(mapView);
        mapView.addMouseListener(mouseEventListener);
        mapView.addMouseMotionListener(mouseEventListener);
        mapView.addMouseWheelListener(mouseEventListener);

        return mapView;
    }

    private static TileCache createTileCache() {
        TileCache firstLevelTileCache = new InMemoryTileCache(64);
        java.io.File cacheDirectory = new java.io.File(FileFactory.createFile(System.getProperty("java.io.tmpdir"), "mapsforge").getAbsolutePath());
        TileCache secondLevelTileCache = new FileSystemTileCache(1024, cacheDirectory, GRAPHIC_FACTORY);
        return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Load Map")) {
            // load Map
            PlatformUIBase.getFile("", "", "Load Map", "Load", new IgetFileReturnListener() {
                @Override
                public void returnFile(String pathAndName) {
                    SimulatorMain.prefs.put("loadedMap", pathAndName);
                    try {
                        SimulatorMain.prefs.flush();
                    } catch (BackingStoreException e) {
                        e.printStackTrace();
                    }

                    addLayers(mapView, pathAndName);
                }
            });
        }
    }

}