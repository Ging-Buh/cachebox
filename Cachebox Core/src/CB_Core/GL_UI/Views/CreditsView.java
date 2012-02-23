package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ScrollView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CreditsView extends GL_View_Base
{

	public CreditsView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		Label test = new Label(100, 100, 300, 50, "Credits_Label");
		test.setFont(Fonts.get22());
		test.setText("Credits View 2");
		test.setHAlignment(HAlignment.CENTER);
		this.addChild(test);

		NinePatch back = new NinePatch(SpriteCache.uiAtlas.findRegion("shaddowrect"), 8, 8, 8, 8);

		ScrollView scrollView = new ScrollView(new CB_RectF(25f, 25f, width - 50, height - 50), this, "CreditsScrollView");
		scrollView.setBackground(back);

		float posY = scrollView.getHeight();
		final float btnHeight = 65;
		int i;
		for (i = 0; i < 10; i++)
		{
			Button btn = new Button(10f, posY - btnHeight - 5, width - 30, btnHeight, this, "Credits_Btn_" + i);
			btn.setText("Button " + i);
			scrollView.addChild(btn);
			Logger.LogCat("ScrollView add Button" + i);
			posY -= btnHeight - 20;
		}
		Logger.LogCat("ScrollView END at " + i);
		this.addChild(scrollView);

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}
