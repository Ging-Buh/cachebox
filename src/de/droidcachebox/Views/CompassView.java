package de.droidcachebox.Views;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Custom_Controls.CompassControl;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CompassView extends FrameLayout implements ViewOptionsMenu,PositionEvent, SelectedCacheEvent {
	private Context context;
	private Cache aktCache;
	private CompassControl compassControl;

	public CompassView(Context context, LayoutInflater inflater) {
		super(context);
		
		SelectedCacheEventList.Add(this);
		PositionEventList.Add(this);
		LinearLayout CompassLayout = (LinearLayout)inflater.inflate(R.layout.compassview, null, false);
		this.addView(CompassLayout);
        
		
		 compassControl = (CompassControl)findViewById(R.id.Compass);
}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			
			aktCache = cache;
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
	public void OnShow() {
		
		
	}

	@Override
	public void OnHide() {
			
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
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, aktCache.Latitude(), aktCache.Longitude());
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;

		
            compassControl.setCompassHeading(heading);
    		compassControl.setCacheHeading(relativeBearing);
        }
	}

	@Override
	public void OrientationChanged(float Testheading) 
	{
		
		if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        {
            Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
            // FillArrow: Luftfahrt
            // Bearing: Luftfahrt
            // Heading: Im Uhrzeigersinn, Geocaching-Norm

            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, aktCache.Latitude(), aktCache.Longitude());
            double relativeBearing = bearing - heading;
         //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;

		
		compassControl.setCompassHeading(heading);
		compassControl.setCacheHeading(relativeBearing);
        }
	}

}
