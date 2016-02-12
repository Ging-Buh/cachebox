package de.CB.Test.Map;

import java.io.File;

import org.mapsforge.map.rendertheme.XmlRenderTheme;

import CB_Locator.CoordinateGPS;
import CB_Locator.Map.CB_InternalRenderTheme;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.MapTileLoader;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL_RotateDrawables;
import CB_Utils.Plattform;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Actions.TestCaseBase;
import de.CB.TestBase.Views.MapView;

public class MapQueueTest extends TestCaseBase {

	private MapView dummyMapView;
	private final MapTileLoader mapTileLoader = new MapTileLoader();
	private Layer layer;

	int X = 70414;
	int Y = 42955;
	int Z = 17;

	Descriptor[] descs = new Descriptor[] { new Descriptor(X, Y + 1, Z, false), new Descriptor(X + 1, Y + 1, Z, false), new Descriptor(X, Y, Z, false), new Descriptor(X + 1, Y, Z, false) };

	private boolean iniMap = true;

	public MapQueueTest() {
		super("MapTileLoader Test", "");
	}

	@Override
	public void work() {

		if (iniMap) {
			dummyMapView = new MapView(this, "TestMapView");

			XmlRenderTheme RenderTheme = CB_InternalRenderTheme.OSMARENDER;
			ManagerBase.Manager.setRenderTheme(RenderTheme);

			String AbsolutePath;
			if (Plattform.used == Plattform.Desktop) {
				AbsolutePath = Gdx.files.internal("assets/pankow.map").file().getAbsolutePath();
			} else {
				// cant Read from Asset use external
				AbsolutePath = new File("storage/extSdCard/GL_RENDER_TEST/pankow.map").getAbsolutePath();
			}

			layer = new Layer(Layer.Type.normal, AbsolutePath, AbsolutePath, AbsolutePath);
			layer.isMapsForge = true;
			ManagerBase.Manager.getLayers().add(layer);
			iniMap = false;

			CoordinateGPS corPankowCenter = new CoordinateGPS(52.82362, 13.77998);
			dummyMapView.setCenter(corPankowCenter);
		}

		mapTileLoader.setLayer(layer);
		mapTileLoader.clearLoadedTiles();
		dummyMapView.SetZoom(17);

		Descriptor lo = new Descriptor(70414, 42955, 17, false);
		Descriptor ru = new Descriptor(70415, 42956, 17, false);

		mapTileLoader.loadTiles(dummyMapView, lo, ru, 17);
	}

	@Override
	public void draw(Batch batch) {
		float u = 0;
		float v = 0;
		CB_List<TileGL_RotateDrawables> rotateList = new CB_List<TileGL_RotateDrawables>();

		for (Descriptor desc : descs) {
			TileGL tile = mapTileLoader.getLoadedTile(desc);
			if (tile != null) {
				tile.canDraw();
				tile.draw(batch, u, v, 512, 512, rotateList);
			}

			u += 512;
			if (u >= 512) {
				v += 512;
				u = 0;
			}
		}

		for (int i = 0, n = rotateList.size(); i < n; i++) {
			TileGL_RotateDrawables drw = rotateList.get(i);
			drw.draw(batch, 0);
		}

	}

	@Override
	public void dispose() {
		mapTileLoader.clearLoadedTiles();

	}

}
