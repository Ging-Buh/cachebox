package org.mapsforge.map.swing.view;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.AwtGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.swing.MapViewer;
import org.mapsforge.map.swing.controller.MapViewComponentListener;
import org.mapsforge.map.swing.util.JavaUtilPreferences;

import CB_Locator.Location;
import CB_Locator.Location.ProviderType;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

public class MapPanel extends JPanel implements ActionListener
{

	private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
	private static final long serialVersionUID = 6067211877479396433L;

	private static AwtMapView mapView;

	public MapPanel()
	{
		mapView = createMapView();
		String MapPath = SimulatorMain.prefs.get("loadedMap", "../germany.map");
		try
		{
			addLayers(mapView, MapPath);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreferencesFacade preferencesFacade = new JavaUtilPreferences(Preferences.userNodeForPackage(MapViewer.class));
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
		GPSData.setLatitude(pos.latitude);
		GPSData.setLongitude(pos.longitude);
		model.mapViewPosition.setCenter(pos);
		model.mapViewPosition.setZoomLevel((byte) SimulatorMain.prefs.getInt("zoom", 16));

		GPSData.addChangeListener(new GPSDataListener()
		{

			@Override
			public void valueChanged()
			{
				if (CB_Locator.Locator.that != null)
				{
					LatLong pos = new LatLong(GPSData.getLatitude(), GPSData.getLongitude());
					model.mapViewPosition.setCenter(pos);
					CB_Locator.Locator.setNewLocation(new Location(pos.latitude, pos.longitude, GPSData.getQuality(), true, (float) GPSData
							.getSpeed(), true, GPSData.getCourse(), GPSData.getAltitude(), ProviderType.GPS));
				}

			}
		});
	}

	private static void addLayers(MapView mapView, String MapPath)
	{
		LayerManager layerManager = mapView.getLayerManager();
		Layers layers = layerManager.getLayers();
		TileCache tileCache = createTileCache();
		layers.clear();
		layers.add(createTileRendererLayer(tileCache, mapView.getModel().mapViewPosition, layerManager, MapPath));
	}

	private static Layer createTileRendererLayer(TileCache tileCache, MapViewPosition mapViewPosition, LayerManager layerManager,
			String MapPath)
	{
		TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapViewPosition, false, GRAPHIC_FACTORY);
		tileRendererLayer.setMapFile(new File(MapPath));
		tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		return tileRendererLayer;
	}

	private static AwtMapView createMapView()
	{
		AwtMapView mapView = new AwtMapView();
		mapView.getFpsCounter().setVisible(true);
		// mapView.addComponentListener(new MapViewComponentListener(mapView,new Dimension(600, 600)));
		mapView.addComponentListener(new MapViewComponentListener(mapView, mapView.getModel().mapViewDimension));

		GpsSimmulatorMouseEventListener mouseEventListener = new GpsSimmulatorMouseEventListener(mapView.getModel());
		mapView.addMouseListener(mouseEventListener);
		mapView.addMouseMotionListener(mouseEventListener);
		mapView.addMouseWheelListener(mouseEventListener);

		LatLong pos = mapView.getModel().mapViewPosition.getCenter();

		return mapView;
	}

	private static TileCache createTileCache()
	{
		TileCache firstLevelTileCache = new InMemoryTileCache(64);
		File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), "mapsforge");
		TileCache secondLevelTileCache = new FileSystemTileCache(1024, cacheDirectory, GRAPHIC_FACTORY);
		return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Load Map"))
		{
			// load Map
			platformConector.getFile("", "", "Load Map", "Load", new IgetFileReturnListner()
			{
				@Override
				public void getFieleReturn(String Path)
				{
					SimulatorMain.prefs.put("loadedMap", Path);
					try
					{
						SimulatorMain.prefs.flush();
					}
					catch (BackingStoreException e)
					{
						e.printStackTrace();
					}

					addLayers(mapView, Path);
				}
			});
		}
	}

}