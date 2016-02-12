package de.CB.Test.Map;

import java.io.File;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.Locator.CompassType;
import CB_Locator.Map.CB_InternalRenderTheme;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_UI_Base.graphics.FontCache;
import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_Utils.Plattform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Actions.TestCaseBase;
import de.CB.TestBase.Views.MainView;
import de.CB.TestBase.Views.MapView;

public class MapViewTest extends TestCaseBase {
	MapView mapView;

	private int Angle;

	public MapViewTest() {
		super("Compleete Mapview", "");

	}

	@Override
	public void work() {
		// Enable Rotate Buttons
		MainView.that.enableRotateButton(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				CB_Button b = (CB_Button) v;
				if (b.getText().equals("CW"))
					Angle -= 5;
				else
					Angle += 5;

				if (Angle < 0)
					Angle = 360 + Angle;
				if (Angle > 360)
					Angle = Angle - 360;

				Locator.setHeading(Angle, CompassType.Magnetic);
				mapView.OrientationChanged();

				return true;
			}
		});
	}

	public void loadMap() {

		XmlRenderTheme RenderTheme = CB_InternalRenderTheme.OSMARENDER;
		ManagerBase.Manager.setRenderTheme(RenderTheme);

		String AbsolutePath;
		if (Plattform.used == Plattform.Desktop) {
			AbsolutePath = Gdx.files.internal("assets/pankow.map").file().getAbsolutePath();
		} else {
			// cant Read from Asset use external
			AbsolutePath = new File("storage/extSdCard/GL_RENDER_TEST/pankow.map").getAbsolutePath();
		}

		Layer newLayer = new Layer(Layer.Type.normal, AbsolutePath, AbsolutePath, AbsolutePath);
		newLayer.isMapsForge = true;
		ManagerBase.Manager.getLayers().add(newLayer);

		mapView.SetCurrentLayer(newLayer);

		MapFileInfo info = ManagerBase.Manager.getMapsforgeLodedMapFileInfo(newLayer);

		if (info != null) {
			LatLong lalo = info.boundingBox.getCenterPoint();
			CoordinateGPS cor = new CoordinateGPS(lalo.getLatitude(), lalo.getLongitude());
			mapView.setCenter(cor);
		}

		mapView.SetNorthOriented(false);

	}

	@Override
	public void draw(Batch batch) {
		if (mapView == null) {
			// Inital Map
			mapView = new MapView(this, "mapView");
			mapView.setPos(0, 0);
			this.addChild(mapView);

			loadMap();

			// Preinitial Fonts
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 20);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 22);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 24);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 26);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 30);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 40);
			FontCache.get(GL_FontFamily.DEFAULT, GL_FontStyle.NORMAL, 50);

		} else {
			mapView.renderChilds(batch, myParentInfo);
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
