package CB_UI.GL_UI.Views.TestViews;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_UI_Base.Energy;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

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

		CB_RectF TextFieldRec = new CB_RectF(0, this.getHeight() - (UI_Size_Base.that.getButtonHeight() * 3),
				UI_Size_Base.that.getButtonWidth() * 6, UI_Size_Base.that.getButtonHeight() * 3);

		wrappedTextField = new CB_UI_Base.GL_UI.Controls.EditTextField(TextFieldRec, this).setWrapType(WrapType.WRAPPED);
		wrappedTextField.setStyle(EditTextField.getDefaultStyle());
		wrappedTextField.setText(splashMsg);
		// wrappedTextField.setText("");

		this.addChild(wrappedTextField);

		// ####################################################

		String test = "Карти";

		char c[] = test.toCharArray();

		Label label = new Label(new CB_RectF(50, 50, 500, 100), "/ExtSD/Карти/Vector Maps");
		this.addChild(label);
		// ####################################################

		// Setting Button
		Button btnSetting = new Button(this.getWidth() - UI_Size_Base.that.getMargin() - (UI_Size_Base.that.getButtonWidthWide() * 2),
				wrappedTextField.getY() - UI_Size_Base.that.getMargin() - UI_Size_Base.that.getButtonHeight(),
				UI_Size_Base.that.getButtonWidthWide() * 2, UI_Size_Base.that.getButtonHeight(), "");

		btnSetting.setText("Post Conection Error");
		btnSetting.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				ConnectionError INSTANCE = new ConnectionError("http:12345");
				GL.that.Toast(INSTANCE);
				return true;
			}
		});

		this.addChild(btnSetting);

		// RadioButton rb = new RadioButton("Test");
		// rb.setPos(50, 50);
		// rb.setWidth(this.getWidth() - rb.getX());
		// rb.setText("Option 1");
		// this.addChild(rb);
		//
		// this.addChild(btnSetting);
		//
		// RadioButton rb2 = new RadioButton("Test");
		// rb2.setPos(50, rb.getMaxY() + UI_Size_Base.that.getMargin());
		// rb2.setWidth(this.getWidth() - rb.getX());
		// rb2.setText("Option 2");
		// this.addChild(rb2);
		//
		// RadioButton rb3 = new RadioButton("Test");
		// rb3.setPos(50, rb2.getMaxY() + UI_Size_Base.that.getMargin());
		// rb3.setWidth(this.getWidth() - rb.getX());
		// rb3.setText("Option 3");
		// this.addChild(rb3);
		//
		// RadioGroup Group = new RadioGroup();
		// Group.add(rb);
		// Group.add(rb2);
		// Group.add(rb3);

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
	protected void render(Batch batch)
	{
		// drawHausVomNikolaus(batch);

		renderDebugInfo(batch);
	}

	String str;

	private void renderDebugInfo(Batch batch)
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

		GL.that.renderOnce();
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
	final Coordinate center = new CoordinateGPS(50.44, 9.28);
	final int drawingWidth = 3000;
	final int drawingHeight = 3000;

	float camerazoom = 10;

	public class result
	{
		public int count;
		public long time;
		public int zoom;
	}

	public static final int MAX_MAP_ZOOM = 22;

	public long getMapTilePosFactor(float zoom)
	{
		long result = 1;
		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
		return result;
	}
}
