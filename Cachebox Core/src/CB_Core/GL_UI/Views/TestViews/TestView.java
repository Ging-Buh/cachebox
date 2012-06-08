package CB_Core.GL_UI.Views.TestViews;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{
	private Button TestButton;
	private SolverDialog solverDialog;

	private CB_Core.GL_UI.libGdx_Controls.TextField textField;
	private CB_Core.GL_UI.libGdx_Controls.WrappedTextField wrappedTextField;

	public static final String br = "¶" + System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";

	private int fontsize = 18;

	Button btn1;
	Button btn2;

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCache.ListBack);

		bf = new BitmapFontCache(Fonts.getBig());
		bf.setText("BF Test", 10, 300);

		// ####################################################

		CB_RectF btnRec = new CB_RectF(0, 200, UiSizes.getButtonWidth() * 2, UiSizes.getButtonHeight());

		btn1 = new Button(btnRec, "");
		btn2 = new Button(btnRec, "");

		btn1.setX(20);
		btn2.setX(btn1.getMaxX());

		this.addChild(btn1);
		this.addChild(btn2);

		btn1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				fontsize--;
				setTTF();
				return true;
			}
		});

		btn2.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				fontsize++;
				setTTF();
				return true;
			}
		});

		setTTF();
		requestLayout();
	}

	void setTTF()
	{

		String costumFontPath = Config.settings.SkinFolder.getValue() + "/calibri.ttf";

		// BitmapFont ttFont = TrueTypeFontFactory.createBitmapFont(Gdx.files.absolute(costumFontPath), FONT_CHARACTERS, fontsize, 7.5f,
		// 1.0f,
		// Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		// ttFont.setColor(1f, 0f, 0f, 1f);

		// FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.absolute(costumFontPath));
		// BitmapFont ttFont = gen.generateFont(44);
		// gen.dispose();
		//
		// ttf = new BitmapFontCache(ttFont);
		// ttf.setText("BF Test", 10, 300);
		// ttf.setColor(Color.RED);

		btn1.setText("--");
		btn2.setText(String.valueOf(fontsize) + "  ++");
	}

	BitmapFontCache bf;
	BitmapFontCache ttf;

	@Override
	protected void render(SpriteBatch batch)
	{
		// drawHausVomNikolaus(batch);

		if (bf != null) bf.draw(batch);
		if (ttf != null) ttf.draw(batch);

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
