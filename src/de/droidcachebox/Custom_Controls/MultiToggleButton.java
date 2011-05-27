package de.droidcachebox.Custom_Controls;



import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import android.view.View.OnClickListener;

public class MultiToggleButton extends Button implements OnClickListener {

	public MultiToggleButton(Context context) {
		super(context);
		
	}

	public MultiToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs,
		        R.styleable.MultiToggleButtonStyle);

		mBackgroundDrawable = a.getDrawable(R.styleable.MultiToggleButtonStyle_MultiToggleButton_Background);
		mBackgroundDrawablePressed = a.getDrawable(R.styleable.MultiToggleButtonStyle_MultiToggleButton_Background_pressed);
		mLedDrawable = a.getDrawable(R.styleable.MultiToggleButtonStyle_MultiToggleButton_Led);
		
		a.recycle();
		
		setOnClickListener(this);
		State.add(new States("off",Color.GRAY));
		setState(0);
	}

	public MultiToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	
	/*
	 *  Private Member
	 */

	private Drawable mBackgroundDrawable;
	private Drawable mBackgroundDrawablePressed;
	private Drawable mLedDrawable;
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		

		
       
        
       setMeasuredDimension(widthSize, heightSize);
       
	}
	
	
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 50;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
	
		 super.onDraw(canvas);
        
		 //canvas.drawColor(Color.RED);
		 int width = getWidth();
         int height = getHeight();
         int ledHeight = 0;
         int ledWidth = 0;
         
        /*
          
         int BackHeight = 0;
         int BackWidth = 0;
         
        if(this.isPressed())
         {
        	 final Drawable finalBackgroundMarkDrawable = mBackgroundDrawablePressed;
 	        if (finalBackgroundMarkDrawable != null) {
 	        	Rect mRect = new Rect();
 	        	
	        	mRect.set(0, 0, width, height);
 	        	finalBackgroundMarkDrawable.setBounds(mRect);
 	        	BackHeight = finalBackgroundMarkDrawable.getIntrinsicHeight();
	        	BackWidth = finalBackgroundMarkDrawable.getIntrinsicWidth();
 	           
 	            finalBackgroundMarkDrawable.draw(canvas);
 	        }
         }
         else
         {
	        final Drawable finalBackgroundMarkDrawable = mBackgroundDrawable;
	        if (finalBackgroundMarkDrawable != null) {
	        	Rect mRect = new Rect();
	        	
	        	mRect.set(0, 0, width, height);
	        	finalBackgroundMarkDrawable.setBounds(mRect);
	        	BackHeight = finalBackgroundMarkDrawable.getIntrinsicHeight();
	        	BackWidth = finalBackgroundMarkDrawable.getIntrinsicWidth();
	           
	            finalBackgroundMarkDrawable.draw(canvas);
	        }
         }  */
	        
         final Drawable finalLed = mLedDrawable;
	        if (finalLed != null) {
	        	Rect mRect = new Rect();
	        	ledHeight = finalLed.getIntrinsicHeight();
	        	ledWidth = finalLed.getIntrinsicWidth();
	        	int left = (width/2)- (ledWidth/2);
	        	int top = height-ledHeight;
	        	PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(aktState.Color, android.graphics.PorterDuff.Mode.MULTIPLY );
	        	 mRect.set(left, top, ledWidth+left , ledHeight+top);
	        	 finalLed.setBounds(mRect);
	        	 
	        	 finalLed.setColorFilter(colorFilter);
	        	 finalLed.draw(canvas);
	        }
		 
			
	}

	
	public class States
	{
		public String Text;
		public int Color;
		
		
		public States(String text, int color)
		{
			Text = text;
			Color = color;
		}
	}

	private ArrayList<States> State = new ArrayList<MultiToggleButton.States>();
	
	public void addState(String Text, int color)
	{
		State.add(new States(Text,color));
	}
	
	public void setState(int ID)
	{
		StateId=ID;
		if(StateId>State.size()-1) StateId = 0;
		aktState = State.get(StateId);
		this.setText(aktState.Text);
		this.invalidate();
	}
	
	public void clearStates()
	{
		State.clear();
	}
	
	private States aktState;
	private int StateId = 0;

	@Override
	public void onClick(View arg0) 
	{
		StateId++;
		setState(StateId);
	}
	
	public int getState()
	{
		return StateId;
	}
	
	public static void initialOn_Off_ToggleStates(MultiToggleButton bt)
	{
		String ButtonTxt = (String) bt.getText();
		bt.clearStates();
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_on));
		
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt,
			String txtOn, String txtOff) 
	{
		bt.clearStates();
		bt.addState(txtOff, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(txtOn, Global.getColor(R.attr.ToggleBtColor_on));
		
	}
	
}
