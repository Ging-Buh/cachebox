package de.droidcachebox.Views;

import CB_Core.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.main;
import de.droidcachebox.Custom_Controls.CacheInfoControl;
import de.droidcachebox.Custom_Controls.CompassControl;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.Custom_Controls.WayPointInfoControl;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import CB_Core.GlobalCore;
import CB_Core.TranslationEngine.SelectedLangChangedEvent;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
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
	private Waypoint aktWaypoint;
	private CompassControl compassControl;
	private CacheInfoControl DescriptionTextView;
	private RelativeLayout ToggleButtonLayout;
	private MultiToggleButton AlignButton;
	private WayPointInfoControl WP_info;
	private Boolean align = false;
	static public int windowW=0;
    static public int windowH=0 ;
    
	public CompassView(Context context, LayoutInflater inflater) 
	{
		super(context);
		
		SelectedCacheEventList.Add(this);
		
		RelativeLayout  CompassLayout = (RelativeLayout )inflater.inflate(R.layout.compassview, null, false);
		this.addView(CompassLayout);
		
		this.setBackgroundColor(Global.getColor(R.attr.myBackground));
		
		 compassControl = (CompassControl)findViewById(R.id.Compass);
		 DescriptionTextView = (CacheInfoControl)findViewById(R.id.CompassDescriptionView);
		 ToggleButtonLayout = (RelativeLayout)findViewById(R.id.layoutCompassToggle);
		 WP_info = (WayPointInfoControl)findViewById(R.id.WaypointDescriptionView);
		 AlignButton = (MultiToggleButton)findViewById(R.id.CompassAlignButton);
		 AlignButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				AlignButton.onClick(arg0);
				align = (AlignButton.getState() == 0)?  false : true;
				
			}
		});
		 
		 DescriptionTextView.setOnClickListener(onDescClick);
		 WP_info.setOnClickListener(onDescClick);
		 
		SelectedLangChangedEventList.Add(this);
		SelectedLangChangedEventCalled();
	}
	
	final View.OnClickListener onDescClick = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			((main) main.mainActivity).showView(2);
			
		}
	};
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
    // we overriding onMeasure because this is where the application gets its right size.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    windowW = getMeasuredWidth();
    windowH = getMeasuredHeight();
    
    DescriptionTextView.setHeight((int) (Global.scaledFontSize_normal * 4.9));
    ToggleButtonLayout.getLayoutParams().height= windowW + 30;

    }
	
	
	

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if ((aktCache != cache) || (aktWaypoint != waypoint))
		{			
			aktCache = cache;
			aktWaypoint = waypoint;
			
			int cacheInfoBackColor = Global.getColor(R.attr.ListBackground_select);
			if (aktWaypoint != null)
            {
				cacheInfoBackColor = Global.getColor(R.attr.ListBackground_secend); // Cache ist nicht selectiert
				WP_info.setWaypoint(aktWaypoint);
				DescriptionTextView.setVisibility(View.GONE);
				WP_info.setVisibility(View.VISIBLE);
            }
			else
			{
				DescriptionTextView.setVisibility(View.VISIBLE);
				WP_info.setVisibility(View.GONE);
				WP_info.setWaypoint(null);
			}
			
			DescriptionTextView.setCache(aktCache, cacheInfoBackColor);
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
	public void OnFree() {
		
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
		if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
        {
            Coordinate position = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
            if(!align)heading = 0; 
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            Coordinate dest = aktCache.Pos;
            float distance = aktCache.Distance(false);
            if (aktWaypoint != null)
            {
            	dest = aktWaypoint.Pos;
            	distance = aktWaypoint.Distance();
            }
            double bearing = Coordinate.Bearing(position, dest);
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;

             		
    		compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(distance));
    		
        }
	}

	@Override
	public void OrientationChanged(float Testheading) 
	{
		
		if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
        {
            Coordinate position = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
            if(!align)heading = 0;
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            Coordinate dest = aktCache.Pos;
            float distance = aktCache.Distance(false);
            if (aktWaypoint != null)
            {
            	dest = aktWaypoint.Pos;
            	distance = aktWaypoint.Distance();
            }
            double bearing = Coordinate.Bearing(position, dest);
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;
            
				
		compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(distance));
		
        }
	}




	@Override
	public void SelectedLangChangedEventCalled() {
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
