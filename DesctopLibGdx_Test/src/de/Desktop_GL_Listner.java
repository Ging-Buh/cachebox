package de;

import java.awt.Point;
import com.badlogic.gdx.InputProcessor;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;

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
//		lastTouchDown=new Point(x, y);
//		aktTouch=new Point(x, y);
//		lastPointer=pointer;
//		lastbutton=button;
//		startLongClickTimer();
//		return this.onTouchDown(x, y, pointer, button) != null;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Events vom Listener nicht behandeln, wir haben unsere eigenes
		// Eventhandling
		return onTouchDraggedBase(x, y, pointer);
	}

	@Override
	public boolean touchMoved(int x, int y) {
//		aktTouch=new Point(x, y);
//		return onTouchDragged(x, y, -1);
		return onTouchDraggedBase(x, y, -1);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return onTouchUpBase(x, y, pointer, button);
//		cancelLongClickTimer();		
//		int tol=5;
//		
//		int minX= lastTouchDown.x-tol;
//		int maxX= lastTouchDown.x+tol;
//		int minY= lastTouchDown.y-tol;
//		int maxY= lastTouchDown.y+tol;
//		
//		
//		//Click detection
//		if(x>minX&&x<maxX&&y>minY&&y<maxY) onClick(x, y, pointer, button);
//		
//		return onTouchUp(x, y, pointer, button);
	}
	
//	Timer timer;
//	
//	private void cancelLongClickTimer()
//	{
//
//		if(timer!=null)
//		{
//			timer.cancel();
//			timer=null;
//		}
//	}
//	
	
//	private void startLongClickTimer()
//	{
//		
//		cancelLongClickTimer();
//		
//		timer = new Timer();
//		TimerTask task = new TimerTask() {
//			@Override
//			public void run() {
//				int tol=5;
//				
//				int minX= lastTouchDown.x-tol;
//				int maxX= lastTouchDown.x+tol;
//				int minY= lastTouchDown.y-tol;
//				int maxY= lastTouchDown.y+tol;
//				
//				
//				//Click detection
//				if(aktTouch.x>minX&&aktTouch.x<maxX&&aktTouch.y>minY&&aktTouch.y<maxY) onLongClick(aktTouch.x, aktTouch.y, lastPointer, lastbutton);
//			}
//		};
//		timer.schedule(task, 2000);
//	}

}
