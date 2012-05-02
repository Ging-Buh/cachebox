package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class LibGdx_Host_Control extends CB_View_Base
{

	private Actor mActor;

	public LibGdx_Host_Control(CB_RectF rec, Actor actor, String Name)
	{
		super(rec, Name);
		mActor = actor;

		mActor.height = rec.getHeight();
		mActor.width = rec.getWidth();

		mActor.x = 0f;
		mActor.y = 0f;

	}

	protected Actor getActor()
	{
		return mActor;
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return mActor.touchDown(x, y, pointer);
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		mActor.touchDragged(x, y, pointer);
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		mActor.touchUp(x, y, pointer);
		return true;
	}

	// ___________________
	protected void render(SpriteBatch batch)
	{
		mActor.draw(batch, 1f);
	}

	public boolean touchMoved(float x, float y)
	{
		return false;
	}

	public boolean scrolled(int amount)
	{
		return false;
	}

	public boolean keyDown(int keycode)
	{
		return false;
	}

	public boolean keyUp(int keycode)
	{
		return false;
	}

	public boolean keyTyped(char character)
	{
		return false;
	}

}
