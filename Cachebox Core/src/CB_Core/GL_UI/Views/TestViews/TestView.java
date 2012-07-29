package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.EditTextField;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{

	private CB_Core.GL_UI.Controls.EditTextField textField;
	private CB_Core.GL_UI.Controls.EditWrapedTextField wrappedTextField;

	public static final String br = "¶" + System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCache.ListBack);

		CB_RectF TextFieldRec = new CB_RectF(0, 150, UiSizes.getButtonWidth() * 6, UiSizes.getButtonHeight() * 3);

		wrappedTextField = new CB_Core.GL_UI.Controls.EditWrapedTextField(TextFieldRec, EditTextField.getDefaultStyle(), "");
		wrappedTextField.setText(splashMsg);
		// wrappedTextField.setText("");

		this.addChild(wrappedTextField);

		// ####################################################

		CB_RectF TextFieldRec2 = new CB_RectF(0, wrappedTextField.getMaxY() + 25, UiSizes.getButtonWidth() * 6,
				UiSizes.getButtonHeight() * 1.1f);

		textField = new EditTextField(TextFieldRec2, EditTextField.getDefaultStyle(), "Test");
		this.addChild(textField);

		requestLayout();

	}

	OnMsgBoxClickListener click = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which)
		{
			// TODO Auto-generated method stub
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

	private void drawHausVomNikolaus(SpriteBatch batch)
	{
		Sprite ArrowSprite = SpriteCache.Arrows.get(5);

		ArrowSprite.setColor(Color.BLACK);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 100, 300, 300);

		ArrowSprite.setColor(Color.RED);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 300, 100, 300);

		ArrowSprite.setColor(Color.BLUE);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 300, 100, 100);

		ArrowSprite.setColor(Color.CYAN);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 100, 300, 100);

		ArrowSprite.setColor(Color.ORANGE);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 100, 300, 300);

		ArrowSprite.setColor(Color.MAGENTA);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 300, 200, 400);

		ArrowSprite.setColor(Color.YELLOW);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 200, 400, 100, 300);
	}

	private void renderDebugInfo(SpriteBatch batch)
	{
		str = "Coursor Pos:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorPos) + "/"
				+ String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugRealCursorPos);
		Fonts.getNormal().draw(batch, str, 20, 120);

		str = "LineCount: " + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugLineCount);
		Fonts.getNormal().draw(batch, str, 20, 100);

		str = "L:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorLine) + " R:"
				+ String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorRow);
		Fonts.getNormal().draw(batch, str, 20, 80);

		String ch = CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCharBeforCursor;

		str = "Vor Cursor: " + ch;
		Fonts.getNormal().draw(batch, str, 20, 60);

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

		GL_Listener.glListener.renderOnce(this.getName() + " requestLayout");
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

}
