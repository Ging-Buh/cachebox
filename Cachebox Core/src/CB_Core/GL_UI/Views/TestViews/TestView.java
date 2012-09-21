package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog;
import CB_Core.GL_UI.Controls.Dialogs.RouteDialog.returnListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{

	private CB_Core.GL_UI.Controls.EditWrapedTextField textField;
	private CB_Core.GL_UI.Controls.EditWrapedTextField wrappedTextField;

	public static final String br = System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCache.ListBack);

		CB_RectF TextFieldRec = new CB_RectF(0, 150, UiSizes.getButtonWidth() * 6, UiSizes.getButtonHeight() * 3);

		wrappedTextField = new CB_Core.GL_UI.Controls.EditWrapedTextField(this, TextFieldRec, EditWrapedTextField.getDefaultStyle(), "",
				EditWrapedTextField.TextFieldType.MultiLineWraped);
		wrappedTextField.setText(splashMsg);
		// wrappedTextField.setText("");

		this.addChild(wrappedTextField);

		// ####################################################

		CB_RectF TextFieldRec2 = new CB_RectF(0, wrappedTextField.getMaxY() + 25, UiSizes.getButtonWidth() * 6, UiSizes.getButtonHeight());

		// CB_RectF TextFieldRec2 = new CB_RectF(0, this.height - (UiSizes.getButtonHeight() * 1.1f), UiSizes.getButtonWidth() * 7,
		// UiSizes.getButtonHeight() * 1.1f);

		textField = new EditWrapedTextField(this, TextFieldRec2, EditWrapedTextField.getDefaultStyle(), "Test",
				EditWrapedTextField.TextFieldType.SingleLine);
		this.addChild(textField);

		// ####################################################
		// CB_RectF GradiantRec = new CB_RectF(10, 40, this.width - 20, UiSizes.getButtonHeight() * 1.5f);
		//
		// GradiantFill fill = new GradiantFill(Color.RED, Color.RED, 0);
		//
		// for (int i = 60; i < 300; i += 60)
		// {
		// HSV_Color c = new HSV_Color(Color.RED);
		// c.setHue(i);
		// GradiantStop stop = new GradiantStop(c.cpy(), (float) i / 360f);
		// fill.addStop(stop);
		// // break;
		// }
		//
		// GradiantFilledRectangle testRec = new GradiantFilledRectangle(GradiantRec, fill);
		// this.addChild(testRec);

		// ####################################################

		// Setting Button
		Button btnSetting = new Button(this.width - Dialog.margin - (UiSizes.getButtonWidthWide() * 2), this.height - Dialog.margin
				- UiSizes.getButtonHeight(), UiSizes.getButtonWidthWide() * 2, UiSizes.getButtonHeight(), "");

		btnSetting.setText("Create Route");
		btnSetting.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				RouteDialog routeDia = new RouteDialog(new returnListner()
				{

					@Override
					public void returnFromRoute_Dialog(boolean canceld, boolean Motoway, boolean CycleWay, boolean FootWay, boolean UseTmc)
					{
						// TODO Auto-generated method stub
						GL_MsgBox.Show("return");
					}

				});

				GL.that.showDialog(routeDia, true);

				return true;
			}
		});

		this.addChild(btnSetting);

		requestLayout();

	}

	OnMsgBoxClickListener click = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which)
		{

			return false;
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
	public void onRezised(CB_RectF rec)
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
