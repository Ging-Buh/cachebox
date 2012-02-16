package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CreditsView extends GL_View_Base
{

	public CreditsView(CB_RectF rec)
	{
		super(rec);
		Label test = new Label(100, 100, 300, 50);
		test.setFont(UiSizes.GL.fontAB22);
		test.setText("Credits View");
		test.setHAlignment(HAlignment.CENTER);
		this.addChild(test);
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
