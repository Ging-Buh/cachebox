package de.droidcachebox.Custom_Controls;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CacheInfoSlider extends Activity implements OnGestureListener
{
	
	private float lastX;
	private float lastY;
	public downSlider slider;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cache_info_slider);
		slider = (downSlider)findViewById(R.id.downSlider);
		
		slider.setOnTouchListener(new OnTouchListener() 
		{
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) 
			{
				
				float distanceY=0;
				
				
				 switch (event.getAction() & MotionEvent.ACTION_MASK) {
			      case MotionEvent.ACTION_DOWN:
			    	 
			    	 lastY= event.getY();
			         
			         break;

			      case MotionEvent.ACTION_MOVE:
			    	  
			    	  distanceY = lastY-event.getY();
			         break;
			      }

				
				
				
				if(distanceY>0)
				{
					
					
			    	lastY= event.getY();
			    	slider.setPos((int) distanceY);
				}
				
				return true;
				
				
			}
		});
		
		
				
		
	}
	
	
	 @Override
	    public boolean onDown(MotionEvent e) {
	        //viewA.setText("-" + "DOWN" + "-");
	        return true;
	    }
	   
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        //viewA.setText("-" + "FLING" + "-");
	        return true;
	    }
	   
	    @Override
	    public void onLongPress(MotionEvent e) {
	        //viewA.setText("-" + "LONG PRESS" + "-");
	    }
	   
	    @Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	        //viewA.setText("-" + "SCROLL" + "-");
	        return true;
	    }
	   
	    @Override
	    public void onShowPress(MotionEvent e) {
	        //viewA.setText("-" + "SHOW PRESS" + "-");
	    }    
	   
	    @Override
	    public boolean onSingleTapUp(MotionEvent e) {
	        //viewA.setText("-" + "SINGLE TAP UP" + "-");
	        return true;
	    }

	
	
   

}
