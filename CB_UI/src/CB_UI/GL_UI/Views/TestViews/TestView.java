package CB_UI.GL_UI.Views.TestViews;

import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.MapTileLoader;
import CB_UI.GlobalCore;
import CB_UI_Base.Energy;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.RadioButton;
import CB_UI_Base.GL_UI.Controls.RadioGroup;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Math.PointD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{

	private CB_UI_Base.GL_UI.Controls.EditTextField wrappedTextField;

	public static final String br = System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCacheBase.ListBack);

		CB_RectF TextFieldRec = new CB_RectF(0, this.height - (UI_Size_Base.that.getButtonHeight() * 3),
				UI_Size_Base.that.getButtonWidth() * 6, UI_Size_Base.that.getButtonHeight() * 3);

		wrappedTextField = new CB_UI_Base.GL_UI.Controls.EditTextField(TextFieldRec, this).setWrapType(WrapType.WRAPPED);
		wrappedTextField.setStyle(EditTextField.getDefaultStyle());
		wrappedTextField.setText(splashMsg);
		// wrappedTextField.setText("");

		this.addChild(wrappedTextField);

		// ####################################################

		// ####################################################

		// Setting Button
		Button btnSetting = new Button(this.width - UI_Size_Base.that.getMargin() - (UI_Size_Base.that.getButtonWidthWide() * 2),
				wrappedTextField.getY() - UI_Size_Base.that.getMargin() - UI_Size_Base.that.getButtonHeight(),
				UI_Size_Base.that.getButtonWidthWide() * 2, UI_Size_Base.that.getButtonHeight(), "");

		btnSetting.setText("Performe Map");
		btnSetting.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				runMapsforgePerformanceTest();
				return true;
			}
		});

		this.addChild(btnSetting);

		RadioButton rb = new RadioButton("Test");
		rb.setPos(50, 50);
		rb.setWidth(this.width - rb.getX());
		rb.setText("Option 1");
		this.addChild(rb);

		this.addChild(btnSetting);

		RadioButton rb2 = new RadioButton("Test");
		rb2.setPos(50, rb.getMaxY() + UI_Size_Base.that.getMargin());
		rb2.setWidth(this.width - rb.getX());
		rb2.setText("Option 2");
		this.addChild(rb2);

		RadioButton rb3 = new RadioButton("Test");
		rb3.setPos(50, rb2.getMaxY() + UI_Size_Base.that.getMargin());
		rb3.setWidth(this.width - rb.getX());
		rb3.setText("Option 3");
		this.addChild(rb3);

		RadioGroup Group = new RadioGroup();
		Group.add(rb);
		Group.add(rb2);
		Group.add(rb3);

		requestLayout();

	}

	Image testImg;

	OnClickListener click = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// if (testImg != null) TestView.this.removeChild(testImg);
			// testImg = new Image(50, 50, 300, 500, "");
			// testImg.setImageURL("http://img.geocaching.com/track/display/2190cf73-ecab-468a-a61a-611c123e567a.jpg");
			// TestView.this.addChild(testImg);
			if (Energy.DisplayOff()) Energy.setDisplayOn();
			else
				Energy.setDisplayOff();
			return true;
		}

	};

	@Override
	protected void render(SpriteBatch batch)
	{
		// drawHausVomNikolaus(batch);

		renderDebugInfo(batch);
	}

	String str;

	private void renderDebugInfo(SpriteBatch batch)
	{
		// str = "Coursor Pos:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorPos) + "/"
		// + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugRealCursorPos);
		// Fonts.getNormal().draw(batch, str, 20, 120);
		//
		// str = "LineCount: " + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugLineCount);
		// Fonts.getNormal().draw(batch, str, 20, 100);
		//
		// str = "L:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorLine) + " R:"
		// + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorRow);
		// Fonts.getNormal().draw(batch, str, 20, 80);
		//
		// String ch = CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCharBeforCursor;
		//
		// str = "Vor Cursor: " + ch;
		// Fonts.getNormal().draw(batch, str, 20, 60);

		// str = "TrackPoi: " + RouteOverlay.AllTrackPoints + " -  " + RouteOverlay.ReduceTrackPoints + " [" + RouteOverlay.DrawedLineCount
		// + "]";
		// Fonts.getNormal().draw(batch, str, 20, 40);
		//
		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 20);

	}

	@Override
	public void onResized(CB_RectF rec)
	{
		requestLayout();
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		this.setSize(rec.getSize());
	}

	private void requestLayout()
	{

		GL.that.renderOnce(this.getName() + " requestLayout");
	}

	@Override
	protected void Initial()
	{

	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	final int mapIntWidth = 3000;
	final int mapIntHeight = 3000;
	final Coordinate center = new Coordinate(50.44, 9.28);
	final int drawingWidth = 3000;
	final int drawingHeight = 3000;

	float camerazoom = 10;

	private void runMapsforgePerformanceTest()
	{
		String result = "";
		for (int i = 7; i < 19; i++)
		{
			result re = runMapsforgePerformanceTest(i);
			result += "Render " + re.count + " Tiles at Zoom " + re.zoom + " in " + re.time + " ms" + GlobalCore.br;
		}

		GL_MsgBox.Show(result);
	}

	public class result
	{
		public int count;
		public long time;
		public int zoom;
	}

	private result runMapsforgePerformanceTest(int aktZoom)
	{
		long start = System.currentTimeMillis();

		Layer layer = ManagerBase.Manager.GetLayerByName("germany", "", "");
		camerazoom = getMapTilePosFactor(aktZoom);
		int tmpzoom = aktZoom;

		int halfMapIntWidth = mapIntWidth / 2;
		int halfMapIntHeight = mapIntHeight / 2;

		int halfDrawingtWidth = drawingWidth / 2;
		int halfDrawingHeight = drawingHeight / 2;

		int ySpeedVersatz = 0;

		Descriptor lo = screenToDescriptor(new Vector2(halfMapIntWidth - halfDrawingtWidth, halfMapIntHeight - halfDrawingHeight
				- ySpeedVersatz), tmpzoom);
		Descriptor ru = screenToDescriptor(new Vector2(halfMapIntWidth + halfDrawingtWidth, halfMapIntHeight + halfDrawingHeight
				+ ySpeedVersatz), tmpzoom);

		int counter = 0;
		for (int i = lo.X; i <= ru.X; i++)
		{
			for (int j = lo.Y; j <= ru.Y; j++)
			{
				Descriptor desc = new Descriptor(i, j, tmpzoom, false);
				ManagerBase.Manager.getMapsforgePixMap(layer, desc);
				counter++;
			}
		}

		result re = new result();
		re.count = counter;
		re.zoom = aktZoom;
		re.time = System.currentTimeMillis() - start;
		return re;
	}

	private Descriptor screenToDescriptor(Vector2 point, int zoom)
	{
		// World-Koordinaten in Pixel
		Vector2 world = screenToWorld(point);
		for (int i = MapTileLoader.MAX_MAP_ZOOM; i > zoom; i--)
		{
			world.x /= 2;
			world.y /= 2;
		}
		world.x /= 256;
		world.y /= 256;
		int x = (int) world.x;
		int y = (int) world.y;
		Descriptor result = new Descriptor(x, y, zoom, false);
		return result;
	}

	private Vector2 screenToWorld(Vector2 point)
	{

		PointD cPoint = Descriptor.ToWorld(Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, center.getLongitude()),
				Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, center.getLatitude()), MapTileLoader.MAX_MAP_ZOOM,
				MapTileLoader.MAX_MAP_ZOOM);

		Vector2 screenCenterW = new Vector2((float) cPoint.X, (float) cPoint.Y);

		Vector2 result = new Vector2(0, 0);
		try
		{

			result.x = screenCenterW.x + ((long) point.x - mapIntWidth / 2) * camerazoom;
			result.y = screenCenterW.y + ((long) point.y - mapIntHeight / 2) * camerazoom;

		}
		catch (Exception e)
		{
			// wenn hier ein Fehler auftritt, dann geben wir einen Vector 0,0 zurück!
		}
		return result;
	}

	public static final int MAX_MAP_ZOOM = 22;

	public long getMapTilePosFactor(float zoom)
	{
		long result = 1;
		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
		return result;
	}
}
