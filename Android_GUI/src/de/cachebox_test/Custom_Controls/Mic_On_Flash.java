package de.cachebox_test.Custom_Controls;


import java.util.TimerTask;

import de.cachebox_test.Global;
import de.cachebox_test.Ui.ActivityUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



public final class Mic_On_Flash extends View 
{

	public Mic_On_Flash(Context context) 
	{
		super(context);
		iniColorArray();
	}

	public Mic_On_Flash(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		iniColorArray();
	}

	public Mic_On_Flash(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		iniColorArray();
	}
	
	
	/*
	 *  Private Member
	 */
	private int height;
	private int width;
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);
		
        
      
        setMeasuredDimension(this.width, this.height);
	}
	
	
	
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) 
    {
        int result = 0;
        
        int specSize = MeasureSpec.getSize(measureSpec);

       
            result = specSize;
        
        
        return result;
    }

    private void iniColorArray()
    {
    	colorArray= new int[42];
    	colorArray[0] = Color.argb(0, 255, 0, 0);
    	for (int i =10; i<255; i+=10)
		{
    		colorArray[i/10] = Color.argb(i, 255, 0, 0);
		}
    	
    	for (int i =25; i<40; i++)
		{
    		colorArray[i] = Color.argb(255, 255, 0, 0);
		}
    	
    	for (int i =40; i<60; i+=10)
		{
    		colorArray[36+(i/10)] = Color.argb(255-i, 255, 0, 0);
		}
    	
    	
    }
    private int[] colorArray;

    
    private int counter=0;
    
	@Override
	protected void onDraw(Canvas canvas) 
	{
	
		ActivityUtils.PutImageTargetHeightColor(canvas,Global.Icons[29],0,0,this.height, colorArray[counter]);
		
	}
	
	Handler handler = new Handler();
	Runnable task = new Runnable() 
	{
		
		@Override
		public void run() 
		{
			Redraw();
		}
	};

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}
	
	public void SetOn()
	{
		this.setVisibility(View.VISIBLE);
		
		handler.postDelayed(task,10); 
		switchOff=false;
		
		
	}
	
	private void Redraw()
	{
		counter++;
		if (counter>colorArray.length-1)
			counter=0;
		this.invalidate();
		if(!switchOff)
			handler.postDelayed(task,10); 
	}
	
	private boolean switchOff=false;
	public void SetOff()
	{
		this.setVisibility(View.GONE);
		switchOff=true;
	}
	
	
	
}
