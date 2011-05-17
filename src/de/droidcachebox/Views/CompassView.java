package de.droidcachebox.Views;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Custom_Controls.CacheInfoControl;
import de.droidcachebox.Custom_Controls.CompassControl;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.SelectedLangChangedEvent;
import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;



public class CompassView extends FrameLayout implements ViewOptionsMenu,PositionEvent, SelectedCacheEvent, SelectedLangChangedEvent {
	private Context context;
	private Cache aktCache;
	private CompassControl compassControl;
	private CacheInfoControl DescriptionTextView;
	private RelativeLayout ToggleButtonLayout;
	private MultiToggleButton AlignButton;
	
	public CompassView(Context context, LayoutInflater inflater) {
		super(context);
		
		SelectedCacheEventList.Add(this);
		
		RelativeLayout  CompassLayout = (RelativeLayout )inflater.inflate(R.layout.compassview, null, false);
		this.addView(CompassLayout);
		
		 compassControl = (CompassControl)findViewById(R.id.Compass);
		 DescriptionTextView = (CacheInfoControl)findViewById(R.id.CompassDescriptionView);
		 ToggleButtonLayout = (RelativeLayout)findViewById(R.id.layoutCompassToggle);
		 AlignButton = (MultiToggleButton)findViewById(R.id.CompassAlignButton);
		 AlignButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				AlignButton.onClick(arg0);
				align = (AlignButton.getState() == 0)?  false : true;
				
			}
		});
		 
		SelectedLangChangedEventList.Add(this);
		SelectedLangChangedEvent();
}
	private Boolean align = false;
	static public int windowW=0;
    static public int windowH=0 ;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
    // we overriding onMeasure because this is where the application gets its right size.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    windowW = getMeasuredWidth();
    windowH = getMeasuredHeight();
    
    DescriptionTextView.setHeight(windowW/3);
    ToggleButtonLayout.getLayoutParams().height= windowW + 20;

    }
	
	
	

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			
			aktCache = cache;
			
			DescriptionTextView.setCache(aktCache, Global.getColor(R.attr.myBackground));
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnShow() 
	{
		PositionEventList.Add(this);
	}

	@Override
	public void OnHide() 
	{
		PositionEventList.Remove(this);
	
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void PositionChanged(Location location) 
	{
		if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        {
            Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
            if(!align)heading = 0; 
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, aktCache.Latitude(), aktCache.Longitude());
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;

             		
    		compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(aktCache.Distance()));
    		
        }
	}

	@Override
	public void OrientationChanged(float Testheading) 
	{
		
		if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        {
            Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
            if(!align)heading = 0;
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, aktCache.Latitude(), aktCache.Longitude());
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;
            
				
		compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(aktCache.Distance()));
		
        }
	}




	@Override
	public void SelectedLangChangedEvent() {
		 AlignButton.clearStates();
		 AlignButton.addState(Global.Translations.Get("Align"), Color.GRAY);
		 AlignButton.addState(Global.Translations.Get("Align"), Color.GREEN);
		 AlignButton.setState(align? 1 : 0);
	}




	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

}
