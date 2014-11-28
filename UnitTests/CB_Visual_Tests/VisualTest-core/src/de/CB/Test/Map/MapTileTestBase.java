package de.CB.Test.Map;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.renderer.GL_DatabaseRenderer;
import org.mapsforge.map.layer.renderer.IDatabaseRenderer;
import org.mapsforge.map.layer.renderer.MixedDatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL_RotateDrawables;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_Utils.Plattform;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Matrix4;

import de.CB.TestBase.Actions.TestCaseBase;
import de.CB.TestBase.Views.MainView;

public abstract class MapTileTestBase extends TestCaseBase
{

	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	private MultiToggleButton togl;

	private static boolean drawMapsforge = false;

	protected boolean CB_TimeAddedToAll = false;
	protected boolean MF_TimeAddedToAll = false;
	String ThemeString;

	// public static final Tile PANKOW = new Tile(35207, 21477, (byte) 16);
	// public static final Tile MALTA = new Tile(4420, 3215, (byte) 13);

	ManagerBase Manager;
	Layer layer;
	TileGL[] GL_Drawable;
	TileGL[] MapsforgeDrawable;
	protected Texture redPix;
	private static MapDatabase GL_mapDatabase = null;
	private static org.mapsforge.map.reader.MapDatabase MF_mapDatabase = null;
	private static IDatabaseRenderer databaseRenderer = null;
	private static IDatabaseRenderer gl_databaseRenderer = null;
	private static File mapFile = null;
	private static File themeFile = null;
	private static final String mapsForgeFile = "";
	private static XmlRenderTheme renderTheme;
	float textScale = 1;
	float DEFAULT_TEXT_SCALE = 1;
	protected String cbTime = "";
	protected String mapsforgeTime = "";

	protected String cbAllTime = "";
	protected String MapsforgeAllTime = "";
	private long lMapsforgeTime = -1;

	private long lCbTime = -1;

	protected static long cbAll = 0;
	protected static long mapsforgeAll = 0;

	private final Tile TILE;

	public MapTileTestBase(Tile tile, boolean deleteTheme)
	{
		super("Draw MapTiles" + br + "Mapsforge", "Cachebox");
		TILE = tile;
		if (deleteTheme) isInitial = false;

	}

	GraphicFactory actFactory;

	private final TileGL[] createCB_Drawable()
	{
		long start1 = System.currentTimeMillis();

		TileGL[] drw = getGlMapDrawables(TILE, GL_Factory);

		lCbTime = System.currentTimeMillis() - start1;
		return drw;
	}

	private final TileGL[] createMapsforge_Drawable()
	{
		// if (true) return null;

		long start = System.currentTimeMillis();
		TileGL[] drw = getMapDrawables(TILE, Mapsforge_Factory);
		lMapsforgeTime = System.currentTimeMillis() - start;

		return drw;
	}

	public long getCbTime()
	{
		return lCbTime;
	}

	public long getMapsforgeTime()
	{
		return lMapsforgeTime;
	}

	protected static boolean isInitial = false;

	@Override
	protected void Initial()
	{
		Manager = ManagerBase.Manager;

		if (Plattform.used == Plattform.Desktop)
		{
			mapFile = Gdx.files.internal("assets/pankow.map").file();
			themeFile = Gdx.files.internal("assets/themes/" + ThemeString).file();
		}
		else
		{
			// cant Read from Asset use external
			mapFile = new File("storage/extSdCard/GL_RENDER_TEST/pankow.map");
			themeFile = new File("storage/extSdCard/GL_RENDER_TEST/themes/" + ThemeString);
		}

		GL_mapDatabase = new MapDatabase();
		GL_mapDatabase.closeFile();
		GL_mapDatabase.openFile(mapFile);
		Logger.DEBUG("Open MapsForge Map: " + mapFile);
		// try
		// {
		// renderTheme = new ExternalRenderTheme(themeFile);
		// }
		// catch (FileNotFoundException e)
		// {
		//
		// }

		GL_mapDatabase.getMapFileInfo();

		if (renderTheme == null) renderTheme = InternalRenderTheme.OSMARENDER;

		gl_databaseRenderer = new GL_DatabaseRenderer(MapTileTestBase.GL_mapDatabase, GL_Factory, GL_dispModel);

		MF_mapDatabase = new org.mapsforge.map.reader.MapDatabase();
		MF_mapDatabase.closeFile();
		MF_mapDatabase.openFile(mapFile);
		Logger.DEBUG("Open MapsForge Map: " + mapFile);
		// try
		// {
		// renderTheme = new ExternalRenderTheme(themeFile);
		// }
		// catch (FileNotFoundException e)
		// {
		//
		// }

		if (renderTheme == null) renderTheme = InternalRenderTheme.OSMARENDER;

		// databaseRenderer = new MF_DatabaseRenderer(MapTileTestBase.MF_mapDatabase, Mapsforge_Factory);
		databaseRenderer = new MixedDatabaseRenderer(MapTileTestBase.MF_mapDatabase, Mapsforge_Factory, 0);

		isInitial = true;
	}

	private float Angle = 0;

	@Override
	public void work()
	{
		synchronized (isDisposed)
		{

			// Enable Rotate Buttons
			MainView.that.enableRotateButton(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					CB_Button b = (CB_Button) v;
					if (b.getText().equals("CW")) Angle -= 5;
					else
						Angle += 5;

					// if (Angle < 0) Angle = 360 - Angle;
					// if (Angle > 360) Angle = Angle - 360;

					return true;
				}
			});
			if (!isInitial) Initial();
			drawCount += 4;

			// GL_Drawable = createCB_Drawable();
			// MapsforgeDrawable = createMapsforge_Drawable();

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					GL_Drawable = createCB_Drawable();
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});

			Thread thread2 = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					MapsforgeDrawable = createMapsforge_Drawable();
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			thread.start();
			thread2.start();
		}

		isDisposed.set(false);
	}

	/**
	 * Gibt vier MapDrawabels zurück
	 * 
	 * @param tile
	 * @return
	 */
	protected TileGL[] getGlMapDrawables(Tile tile, GraphicFactory factory)
	{
		try
		{

			int X = (int) tile.tileX;
			int Y = (int) tile.tileY;
			byte Z = tile.zoomLevel;

			Tile[] tiles = new Tile[]
				{ new Tile(X, Y, Z), new Tile(X + 1, Y, Z), new Tile(X, Y + 1, Z), new Tile(X + 1, Y + 1, Z) };

			TileGL[] ret = new TileGL[4];
			RendererJob jobs[] = new RendererJob[4];
			org.mapsforge.map.layer.renderer.RendererJob MFjobs[] = new org.mapsforge.map.layer.renderer.RendererJob[4];

			// GL Renderer

			int index = 0;
			for (Tile ti : tiles)
			{

				jobs[index++] = new RendererJob(ti, mapFile, renderTheme, GL_dispModel, textScale, false);
			}

			index = 0;
			for (RendererJob job : jobs)
			{
				ret[index++] = gl_databaseRenderer.execute(job);
			}

			return ret;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gibt vier MapDrawabels zurück
	 * 
	 * @param tile
	 * @return
	 */
	protected TileGL[] getMapDrawables(Tile tile, GraphicFactory factory)
	{
		try
		{

			int X = (int) tile.tileX;
			int Y = (int) tile.tileY;
			byte Z = tile.zoomLevel;

			Tile[] tiles = new Tile[]
				{ new Tile(X, Y, Z), new Tile(X + 1, Y, Z), new Tile(X, Y + 1, Z), new Tile(X + 1, Y + 1, Z) };

			TileGL[] ret = new TileGL[4];
			RendererJob jobs[] = new RendererJob[4];
			org.mapsforge.map.layer.renderer.RendererJob MFjobs[] = new org.mapsforge.map.layer.renderer.RendererJob[4];

			// Mapsforge Renderer

			int index = 0;
			for (Tile ti : tiles)
			{

				MFjobs[index++] = new org.mapsforge.map.layer.renderer.RendererJob(ti, mapFile, renderTheme, dispModel, textScale, false);
			}

			index = 0;
			for (org.mapsforge.map.layer.renderer.RendererJob job : MFjobs)
			{
				ret[index++] = databaseRenderer.execute(job);
			}
			return ret;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	static boolean first = false;

	static int drawCount = 0;

	@Override
	public void draw(Batch batch)
	{
		synchronized (isDisposed)
		{

			if (isDisposed.get()) return;
			if (togl != null)
			{
				if (this.getWidth() > this.getHeight())
				{
					togl.setPos(this.getWidth() - 10 - togl.getWidth(), 10);
				}
				else
				{
					togl.setPos(10, 10);
				}
			}
			else
			{
				togl = new MultiToggleButton("TGL");

				MultiToggleButton.initialOn_Off_ToggleStates(togl);

				togl.setSize(togl.getHeight() * 2, togl.getHeight() * 2);

				togl.setOnStateChangedListner(new OnStateChangeListener()
				{

					@Override
					public void onStateChange(GL_View_Base v, int State)
					{
						drawMapsforge = !drawMapsforge;
						botomMsg = null;
						topMsg = null;
					}
				});

				this.addChild(togl);
			}

			boolean readyTest = true;

			float x = firstPoint.x;
			float y = (firstPoint.y + 1);

			float TILESIZE = GL_dispModel.getTileSize();

			// TILESIZE *= 1.5f;
			// TILESIZE /= 2;

			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

			int u = (int) intersectRec.getX();
			int v = (int) intersectRec.getY();
			Gdx.gl.glScissor((int) (u + x), (int) (v + y), (int) (TILESIZE * 2), (int) (TILESIZE * 2));

			if (GL_Drawable != null && !drawMapsforge)
			{
				batch.end();
				Matrix4 matrixOri = new Matrix4(batch.getProjectionMatrix());
				Matrix4 matrix = batch.getProjectionMatrix();
				matrix.translate(x + TILESIZE, y + TILESIZE, 0);
				// matrix.rotate(0, 0, 1, Angle);
				matrix.translate(-(x + TILESIZE), -(y + TILESIZE), 0);
				batch.begin();

				CB_List<TileGL_RotateDrawables> rotateList = new CB_List<TileGL_RotateDrawables>();

				if (GL_Drawable[2] != null) GL_Drawable[2].draw(batch, x, y, TILESIZE, TILESIZE, rotateList);
				else
					readyTest = false;
				if (GL_Drawable[3] != null) GL_Drawable[3].draw(batch, x + TILESIZE, y, TILESIZE, TILESIZE, rotateList);
				else
					readyTest = false;
				if (GL_Drawable[0] != null) GL_Drawable[0].draw(batch, x, y + TILESIZE, TILESIZE, TILESIZE, rotateList);
				else
					readyTest = false;
				if (GL_Drawable[1] != null) GL_Drawable[1].draw(batch, x + TILESIZE, y + TILESIZE, TILESIZE, TILESIZE, rotateList);
				else
					readyTest = false;

				for (int i = 0, n = rotateList.size(); i < n; i++)
				{
					TileGL_RotateDrawables drw = rotateList.get(i);
					drw.draw(batch, -Angle);
				}

				batch.end();
				batch.setProjectionMatrix(matrixOri);
				batch.begin();
			}

			// x = (int) secondPoint.x;
			// y = (int) (secondPoint.y + 1);

			x = (int) firstPoint.x;
			y = (int) (firstPoint.y + 1);

			if (MapsforgeDrawable != null && drawMapsforge)
			{
				batch.end();
				Matrix4 matrixOri = new Matrix4(batch.getProjectionMatrix());
				Matrix4 matrix = batch.getProjectionMatrix();
				matrix.translate(x + TILESIZE, y + TILESIZE, 0);
				matrix.rotate(0, 0, 1, Angle);
				matrix.translate(-(x + TILESIZE), -(y + TILESIZE), 0);
				batch.begin();

				CB_List<TileGL_RotateDrawables> rotateList = new CB_List<TileGL_RotateDrawables>();

				if (MapsforgeDrawable[2] != null)
				{
					MapsforgeDrawable[2].canDraw();
					MapsforgeDrawable[2].draw(batch, x, y, TILESIZE, TILESIZE, rotateList);
				}
				else
					readyTest = false;
				if (MapsforgeDrawable[3] != null)
				{
					MapsforgeDrawable[3].canDraw();
					MapsforgeDrawable[3].draw(batch, x + TILESIZE, y, TILESIZE, TILESIZE, rotateList);
				}
				else
					readyTest = false;
				if (MapsforgeDrawable[0] != null)
				{
					MapsforgeDrawable[0].canDraw();
					MapsforgeDrawable[0].draw(batch, x, y + TILESIZE, TILESIZE, TILESIZE, rotateList);
				}
				else
					readyTest = false;
				if (MapsforgeDrawable[1] != null)
				{
					MapsforgeDrawable[1].canDraw();
					MapsforgeDrawable[1].draw(batch, x + TILESIZE, y + TILESIZE, TILESIZE, TILESIZE, rotateList);
				}
				else
					readyTest = false;

				for (int i = 0, n = rotateList.size(); i < n; i++)
				{
					TileGL_RotateDrawables drw = rotateList.get(i);
					drw.draw(batch, -Angle);
				}

				batch.end();
				batch.setProjectionMatrix(matrixOri);
				batch.begin();
			}

			if (!CB_TimeAddedToAll)
			{
				try
				{
					if (GL_Drawable != null && GL_Drawable[0] != null && GL_Drawable[1] != null && GL_Drawable[2] != null
							&& GL_Drawable[3] != null)
					{

						cbTime = "@" + getCbTime() + "ms";
						if (drawCount > 0) cbAll += getCbTime();
						cbAllTime = "@all " + cbAll + "ms";
						CB_TimeAddedToAll = true;
					}
					else
					{
						// if (GL_Drawable != null) System.out.print("FBO noch nicht erstellt");
						readyTest = false;
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (!MF_TimeAddedToAll)
			{
				if (MapsforgeDrawable != null && MapsforgeDrawable[0] != null && MapsforgeDrawable[1] != null
						&& MapsforgeDrawable[2] != null && MapsforgeDrawable[3] != null)
				{

					mapsforgeTime = "@" + getMapsforgeTime() + "ms";
					if (drawCount > 0) mapsforgeAll += getMapsforgeTime();
					MapsforgeAllTime = "@all " + mapsforgeAll + "ms";
					MF_TimeAddedToAll = true;

				}
				else
					readyTest = false;
			}

			Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth(),
					(int) intersectRec.getHeight());

			if (this.getWidth() > this.getHalfHeight())
			{
				float yPos = this.getHalfHeight() + 150;
				float CB_X = TILESIZE * 2 + 10;
				float MF_X = TILESIZE * 2 + 10;

				BitmapFont font = Fonts.getNormal();

				font.setColor(COLOR.getFontColor());

				font.draw(batch, mapsforgeTime, MF_X, yPos - 30);
				font.draw(batch, MapsforgeAllTime, MF_X, yPos - 60);

				yPos = 150;

				font.draw(batch, cbTime, CB_X, yPos - 30);
				font.draw(batch, cbAllTime, CB_X, yPos - 60);

				// Draw MSG
				if (topMsg == null)
				{

					topMsg = new BitmapFontCache(drawMapsforge ? Fonts.getBig() : Fonts.getNormal());
					topMsg.setColor(drawMapsforge ? Color.RED : Color.BLACK);
					topMsg.setMultiLineText(MSG_TOP, MF_X, getHeight() - 20);

				}

				if (botomMsg == null)
				{

					botomMsg = new BitmapFontCache(drawMapsforge ? Fonts.getNormal() : Fonts.getBig());
					botomMsg.setColor(drawMapsforge ? Color.BLACK : Color.RED);
					botomMsg.setMultiLineText(MSG_BOTOM, CB_X, getHalfHeight() - 20);

				}

			}
			else
			{

			}

			isReady = readyTest;
		}
	}

	@Override
	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;

			if (GL_Drawable != null)
			{
				for (TileGL drw : GL_Drawable)
				{
					if (drw != null) drw.dispose();

				}

				GL_Drawable = null;
			}

			if (MapsforgeDrawable != null)
			{
				MapsforgeDrawable = null;
			}

			if (MapsforgeDrawable != null)
			{
				for (TileGL drw : MapsforgeDrawable)
				{
					if (drw != null)
					{
						drw.dispose();
					}

				}

				MapsforgeDrawable = null;
			}
			isReady = false;
			isDisposed.set(true);
		}

	}

}
