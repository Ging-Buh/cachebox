package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MapInfoPanel;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;

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
	private Image image;
	private Label lbl;
	private ZoomButtons btnZoom;
	private MultiToggleButton togBtn;
	private MapInfoPanel info;

	private CB_Core.GL_UI.libGdx_Controls.Button button;
	private CB_Core.GL_UI.libGdx_Controls.TextField textField;

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		CB_RectF r = new CB_RectF(100, 500, 100, 100);
		button = new CB_Core.GL_UI.libGdx_Controls.Button(r, "TestButton", SpriteCache.Misc);
		this.addChild(button);

		CB_RectF r2 = new CB_RectF(20, 450, 400, 40);
		textField = new TextField(r2, "TestTextField");
		this.addChild(textField);

		textField.setText(" Das ist ein Text fürs Text Feld");
		textField.setSelection(3, 9);
		textField.setCursorPosition(17);

		requestLayout();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		String str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 100);

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

}
