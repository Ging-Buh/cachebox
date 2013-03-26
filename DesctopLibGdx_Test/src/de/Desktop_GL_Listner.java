package de;

import CB_Core.Events.KeyCodes;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.InputProcessor;

public class Desktop_GL_Listner extends Tab_GL_Listner implements InputProcessor
{

	public Desktop_GL_Listner(int initalWidth, int initialHeight)
	{
		super(initalWidth, initialHeight);

	}

	// # ImputProzessor Implamantations

	// private Point lastTouchDown;
	// private Point aktTouch;
	// private int lastPointer;
	// private int lastbutton;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		return this.onTouchDownBase(x, y, pointer, button);

	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		// Events vom Listener nicht behandeln, wir haben unsere eigenes
		// Eventhandling
		return onTouchDraggedBase(x, y, pointer);
	}

	private int MouseX = 0;
	private int MouseY = 0;

	@Override
	public boolean mouseMoved(int x, int y)
	{
		MouseX = x;
		MouseY = y;
		return onTouchDraggedBase(x, y, -1);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		return onTouchUpBase(x, y, pointer, button);

	}

	// private Stage mStage;

	// private void chkStageInitial()
	// {
	// if (mStage == null)
	// {// initial a virtual stage
	// mStage = new Stage(UiSizes.getWindowWidth(), UiSizes.getWindowHeight(), false);
	// }
	// }

	@Override
	public boolean keyTyped(char character)
	{
		if (DialogIsShown && character == KeyCodes.KEYCODE_BACK)
		{
			closeDialog(mDialog);
			return true; // behandelt!
		}

		if (ActivityIsShown && character == KeyCodes.KEYCODE_BACK)
		{
			closeActivity();
			return true; // behandelt!
		}

		// WeiterLeiten an EditTextView, welches den Focus Hat
		if (keyboardFocus != null && keyboardFocus.keyTyped(character)) return true;

		return false;

	}

	@Override
	public boolean keyUp(int KeyCode)
	{
		// WeiterLeiten an EditTextView, welches den Focus Hat
		if (keyboardFocus != null && keyboardFocus.keyUp(KeyCode)) return true;
		return false;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		// WeiterLeiten an EditTextView, welches den Focus Hat
		if (keyboardFocus != null && keyboardFocus.keyDown(keycode)) return true;
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{

		int scrollSize = (UiSizes.that.getClickToleranz() + 10) * amount;

		int Pointer = (scrollSize > 0) ? GL_View_Base.MOUSE_WHEEL_POINTER_UP : GL_View_Base.MOUSE_WHEEL_POINTER_DOWN;

		this.onTouchDownBase(MouseX, MouseY, Pointer, -1);

		this.onTouchDraggedBase(MouseX - scrollSize, MouseY - scrollSize, Pointer);

		this.onTouchUpBase(MouseX - scrollSize, MouseY - scrollSize, Pointer, -1);

		return true;
	}

}
