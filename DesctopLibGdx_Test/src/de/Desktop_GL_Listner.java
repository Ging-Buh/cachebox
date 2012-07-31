package de;

import java.awt.Point;
import com.badlogic.gdx.InputProcessor;

import CB_Core.Events.KeyCodes;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.GL_UI.libGdx_Controls.LibGdx_Host_Control;

public class Desktop_GL_Listner extends Tab_GL_Listner implements InputProcessor {

	public Desktop_GL_Listner(int initalWidth, int initialHeight) {
		super(initalWidth, initialHeight);


	}

	
	
	
	// # ImputProzessor Implamantations


	
	private Point lastTouchDown;
	private Point aktTouch;
	private int lastPointer;
	private int lastbutton;
	
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		return this.onTouchDownBase(x, y, pointer, button);

	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Events vom Listener nicht behandeln, wir haben unsere eigenes
		// Eventhandling
		return onTouchDraggedBase(x, y, pointer);
	}

	@Override
	public boolean mouseMoved(int x, int y) {
//		aktTouch=new Point(x, y);
//		return onTouchDragged(x, y, -1);
		return onTouchDraggedBase(x, y, -1);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return onTouchUpBase(x, y, pointer, button);

	}
	
		
	
	@Override
	public boolean keyTyped (char character) 
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

		// WeiterLeiten an VirtualStage!
		return LibGdx_Host_Control.keyTyped(character);

	}

	@Override
	public boolean keyUp(int KeyCode)
	{
		// WeiterLeiten an EditTextView, welches den Focus Hat
		if (keyboardFocus != null && keyboardFocus.keyUp(KeyCode)) return true;
		return LibGdx_Host_Control.keyUp(KeyCode);
	}

	@Override
	public boolean keyDown(int keycode)
	{
		// WeiterLeiten an EditTextView, welches den Focus Hat
		if (keyboardFocus != null && keyboardFocus.keyDown(keycode)) return true;
		return LibGdx_Host_Control.keyDown(keycode);
	}

	


}
