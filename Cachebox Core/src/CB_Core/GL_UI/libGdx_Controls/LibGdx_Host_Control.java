package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public abstract class LibGdx_Host_Control extends CB_View_Base
{

	private com.badlogic.gdx.scenes.scene2d.ui.TextField mActor;

	private static Stage mStage;

	public LibGdx_Host_Control(CB_RectF rec, com.badlogic.gdx.scenes.scene2d.ui.TextField actor, String Name)
	{

		super(rec, Name);

		chkStageInitial();

		mActor = actor;

		mActor.setHeight(rec.getHeight());
		mActor.setWidth(rec.getWidth());

		mActor.setX(0f);
		mActor.setY(0f);

		mStage.addActor(mActor);

	}

	private static void chkStageInitial()
	{
		if (mStage == null)
		{// initial a virtual stage
			mStage = new Stage(UiSizes.getWindowWidth(), UiSizes.getWindowHeight(), false);
		}
	}

	public Actor getActor()
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
		float offset = 0;

		if (mActor instanceof com.badlogic.gdx.scenes.scene2d.ui.TextField)
		{
			offset = -((com.badlogic.gdx.scenes.scene2d.ui.TextField) mActor).getStyle().background.getLeftWidth();
		}

		if (mActor instanceof CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField)
		{
			offset = -((CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField) mActor).getStyle().background.getLeftWidth();
		}

		chkInputListner();
		if (inputEventListner != null) return inputEventListner.touchDown(null, x + offset, y, pointer, 0);

		return true;
	}

	InputListener inputEventListner;

	private void chkInputListner()
	{
		if (inputEventListner != null) return;

		Array<EventListener> listners = mActor.getListeners();

		for (EventListener listner : listners)
		{
			if (listner instanceof InputListener) inputEventListner = (InputListener) listner;
		}

	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		float offset = 0;

		if (mActor instanceof com.badlogic.gdx.scenes.scene2d.ui.TextField)
		{
			offset = -((com.badlogic.gdx.scenes.scene2d.ui.TextField) mActor).getStyle().background.getLeftWidth();
		}

		if (mActor instanceof CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField)
		{
			offset = -((CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField) mActor).getStyle().background.getLeftWidth();
		}

		chkInputListner();
		if (inputEventListner != null) inputEventListner.touchDragged(null, x + offset, y, pointer);

		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		float offset = 0;

		if (mActor instanceof com.badlogic.gdx.scenes.scene2d.ui.TextField)
		{
			offset = -((com.badlogic.gdx.scenes.scene2d.ui.TextField) mActor).getStyle().background.getLeftWidth();
		}

		if (mActor instanceof CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField)
		{
			offset = -((CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField) mActor).getStyle().background.getLeftWidth();
		}

		chkInputListner();
		if (inputEventListner != null) inputEventListner.touchUp(null, x + offset, y, pointer, 0);

		return true;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		if (mActor instanceof WrappedTextField)
		{
			((WrappedTextField) mActor).click(x, y, pointer, button);
		}

		return false;
	}

	// ___________________
	protected void render(SpriteBatch batch)
	{
		mActor.draw(batch, 1f);
	}

	private boolean touchMoved(float x, float y)
	{
		chkStageInitial();
		return mStage.mouseMoved((int) x, (int) y);
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
