package de;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;

public class Desktop_GL_Listner extends GL_Listener  implements InputProcessor
{

	public Desktop_GL_Listner(int initalWidth, int initialHeight) {
		super(initalWidth, initialHeight);
		
		
		
	}

	
	// # ImputProzessor Implamantations

			@Override
			public boolean keyDown(int arg0)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean keyTyped(char arg0)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean keyUp(int arg0)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean scrolled(int arg0)
			{
				// TODO Auto-generated method stub
				return false;
			}
		
		
			@Override
			public boolean touchDown(int x, int y, int pointer, int button)
			{
				return this.onTouchDown( x,  y,  pointer,  button);
			}

			@Override
			public boolean touchDragged(int x, int y, int pointer)
			{
				// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
				return false;
			}

			@Override
			public boolean touchMoved(int x, int y)
			{
				// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
				return onTouchDragged( x,  y,-1);
			}

			@Override
			public boolean touchUp(int x, int y, int pointer, int button)
			{
				return onTouchUp( x,  y,  pointer,  button);
			}
	
	
}
