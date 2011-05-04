package de.droidcachebox;

import android.graphics.Color;
import android.graphics.Paint;

public class CBColors {
 	public int TitleBarColor;
    public int TitleBarText;    
   	public int ListBackground;
   	public int ListSeperator;
   	public int EmptyBackground;
   	public int Foreground;
   	public int SelectedBackground;
   	public int ControlColorFilter;
   	public int ColorCompassPanel;
   	public int ColorCompassText;
   	public Paints Paints = new Paints();
   	
   	class Paints {
		public Paint ListSeperator;
		public Paint selectedBack;   
	    public Paint ListBackground;    		
   	}
}
