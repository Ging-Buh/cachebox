package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class LibGdx_Host_Control extends CB_View_Base
{

	private Actor mActor;

	private static Stage mStage;

	public LibGdx_Host_Control(CB_RectF rec, Actor actor, String Name)
	{

		super(rec, Name);

		chkStageInitial();

		mActor = actor;

		mActor.height = rec.getHeight();
		mActor.width = rec.getWidth();

		mActor.x = 0f;
		mActor.y = 0f;

		mStage.addActor(mActor);

	}

	private static void chkStageInitial()
	{
		if (mStage == null)
		{// initial a virtual stage
			mStage = new Stage(UiSizes.getWindowWidth(), UiSizes.getWindowHeight(), false);
		}
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

	public static boolean touchMoved(float x, float y)
	{
		chkStageInitial();
		return mStage.touchMoved((int) x, (int) y);
	}

	public static boolean scrolled(int amount)
	{
		chkStageInitial();
		return mStage.scrolled(amount);
	}

	public static boolean keyDown(int keycode)
	{
		chkStageInitial();
		return mStage.keyDown(keycode);
	}

	public static boolean keyUp(int keycode)
	{
		chkStageInitial();
		return mStage.keyUp(keycode);
	}

	public static boolean keyTyped(char character)
	{
		chkStageInitial();
		return mStage.keyTyped(character);
	}

}
