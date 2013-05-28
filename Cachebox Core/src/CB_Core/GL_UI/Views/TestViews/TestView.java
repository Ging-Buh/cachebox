package CB_Core.GL_UI.Views.TestViews;

import CB_Core.Energy;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.TB_Details;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.RadioButton;
import CB_Core.GL_UI.Controls.RadioGroup;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Types.Trackable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{

	private CB_Core.GL_UI.Controls.EditWrapedTextField wrappedTextField;

	public static final String br = System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCache.ListBack);

		CB_RectF TextFieldRec = new CB_RectF(0, this.height - (UI_Size_Base.that.getButtonHeight() * 3),
				UI_Size_Base.that.getButtonWidth() * 6, UI_Size_Base.that.getButtonHeight() * 3);

		wrappedTextField = new CB_Core.GL_UI.Controls.EditWrapedTextField(this, TextFieldRec, EditWrapedTextField.getDefaultStyle(), "",
				EditWrapedTextField.TextFieldType.MultiLineWraped);
		wrappedTextField.setText(splashMsg);
		// wrappedTextField.setText("");

		this.addChild(wrappedTextField);

		// ####################################################

		// ####################################################

		// Setting Button
		Button btnSetting = new Button(this.width - Dialog.getMargin() - (UI_Size_Base.that.getButtonWidthWide() * 2),
				wrappedTextField.getY() - Dialog.getMargin() - UI_Size_Base.that.getButtonHeight(),
				UI_Size_Base.that.getButtonWidthWide() * 2, UI_Size_Base.that.getButtonHeight(), "");

		btnSetting.setText("Show TB");
		btnSetting.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				String beschreibung = "Das ist die Beschreibung des TB's, welche noch aus HTML formatiert werden muss!";

				Trackable tb = new Trackable("MyTb", "http://www.geocaching.com/images/wpttypes/21.gif", beschreibung);

				if (TB_Details.that == null) new TB_Details();
				TB_Details.that.Show(tb);
				return true;
			}
		});

		btnSetting.setOnClickListener(click);

		this.addChild(btnSetting);

		RadioButton rb = new RadioButton("Test");
		rb.setPos(50, 50);
		rb.setWidth(this.width - rb.getX());
		rb.setText("Option 1");
		this.addChild(rb);

		this.addChild(btnSetting);

		RadioButton rb2 = new RadioButton("Test");
		rb2.setPos(50, rb.getMaxY() + Dialog.getMargin());
		rb2.setWidth(this.width - rb.getX());
		rb2.setText("Option 2");
		this.addChild(rb2);

		RadioButton rb3 = new RadioButton("Test");
		rb3.setPos(50, rb2.getMaxY() + Dialog.getMargin());
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

}
