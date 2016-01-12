package de.CB.TestBase.Actions;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.reader.header.MapFileInfo;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.CB.TestBase.Views.MainView;
import de.CB.TestBase.Views.MapView;

public class CB_Action_ShowMap extends CB_Action_ShowView
{

	public CB_Action_ShowMap()
	{
		super("Map", MenuID.AID_SHOW_MAP);
	}

	@Override
	public void Execute()
	{
		if ((MainView.mapView == null) && (tabMainView != null) && (tab != null)) MainView.mapView = new MapView(tab.getContentRec(),
				"MapView");

		if ((MainView.mapView != null) && (tab != null)) tab.ShowView(MainView.mapView);
	}

	@Override
	public CB_View_Base getView()
	{
		return MainView.mapView;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.map_5.ordinal());
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		PlatformConnector.getFile("", "", "", "", new IgetFileReturnListener()
		{

			@Override
			public void getFileReturn(String arg0)
			{
				loadMap(arg0);
			}
		});
		return null;
	}

	public void loadMap(String AbsolutePath)
	{
		Layer newLayer = new Layer(Layer.Type.normal, AbsolutePath, AbsolutePath, AbsolutePath);
		newLayer.isMapsForge = true;
		ManagerBase.Manager.getLayers().add(newLayer);

		MainView.mapView.SetCurrentLayer(newLayer);

		MapFileInfo info = ManagerBase.Manager.getMapsforgeLodedMapFileInfo(newLayer);

		if (info != null)
		{
			LatLong lalo = info.boundingBox.getCenterPoint();
			CoordinateGPS cor = new CoordinateGPS(lalo.getLatitude(), lalo.getLongitude());
			MainView.mapView.setCenter(cor);
		}

	}

}
