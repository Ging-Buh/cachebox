package de;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;

public class Desktop_GL_Listner extends GL_Listener implements InputProcessor {

	public Desktop_GL_Listner(int initalWidth, int initialHeight) {
		super(initalWidth, initialHeight);

		GL_View_Base.debug = true;
		GL_View_Base.disableScissor = true;

	}

	// # ImputProzessor Implamantations

	@Override
	public boolean keyDown(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private Point lastTouchDown;
	private Point aktTouch;
	private int lastPointer;
	private int lastbutton;
	
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		lastTouchDown=new Point(x, y);
		aktTouch=new Point(x, y);
		lastPointer=pointer;
		lastbutton=button;
		startLongClickTimer();
		return this.onTouchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Events vom Listener nicht behandeln, wir haben unsere eigenes
		// Eventhandling
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		aktTouch=new Point(x, y);
		return onTouchDragged(x, y, -1);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		cancelLongClickTimer();		
		int tol=5;
		
		int minX= lastTouchDown.x-tol;
		int maxX= lastTouchDown.x+tol;
		int minY= lastTouchDown.y-tol;
		int maxY= lastTouchDown.y+tol;
		
		
		//Click detection
		if(x>minX&&x<maxX&&y>minY&&y<maxY) onClick(x, y, pointer, button);
		
		return onTouchUp(x, y, pointer, button);
	}
	
	Timer timer;
	
	private void cancelLongClickTimer()
	{

		if(timer!=null)
		{
			timer.cancel();
			timer=null;
		}
	}
	
	
	private void startLongClickTimer()
	{
		
		cancelLongClickTimer();
		
		timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int tol=5;
				
				int minX= lastTouchDown.x-tol;
				int maxX= lastTouchDown.x+tol;
				int minY= lastTouchDown.y-tol;
				int maxY= lastTouchDown.y+tol;
				
				
				//Click detection
				if(aktTouch.x>minX&&aktTouch.x<maxX&&aktTouch.y>minY&&aktTouch.y<maxY) onLongClick(aktTouch.x, aktTouch.y, lastPointer, lastbutton);
			}
		};
		timer.schedule(task, 2000);
	}

}
