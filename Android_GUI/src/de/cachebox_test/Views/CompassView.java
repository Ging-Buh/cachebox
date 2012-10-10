package de.cachebox_test.Views;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.QuickButtonList;
import CB_Core.Math.UiSizes;
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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.CacheInfoControl;
import de.cachebox_test.Custom_Controls.CompassControl;
import de.cachebox_test.Custom_Controls.MultiToggleButton;
import de.cachebox_test.Custom_Controls.WayPointInfoControl;
import de.cachebox_test.Events.PositionEvent;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Events.ViewOptionsMenu;

public class CompassView extends FrameLayout implements ViewOptionsMenu, PositionEvent, SelectedLangChangedEvent, SelectedCacheEvent
{
	private Cache aktCache;
	private Waypoint aktWaypoint;
	private CompassControl compassControl;
	private CacheInfoControl DescriptionTextView;
	private RelativeLayout ToggleButtonLayout;
	private MultiToggleButton AlignButton;
	private WayPointInfoControl WP_info;
	private Boolean align = false;

	public CompassView(Context context, LayoutInflater inflater)
	{
		super(context);

		RelativeLayout CompassLayout = (RelativeLayout) inflater.inflate(R.layout.compassview, null, false);
		this.addView(CompassLayout);

		this.setBackgroundColor(Global.getColor(R.attr.myBackground));

		compassControl = (CompassControl) findViewById(R.id.Compass);
		DescriptionTextView = (CacheInfoControl) findViewById(R.id.CompassDescriptionView);
		ToggleButtonLayout = (RelativeLayout) findViewById(R.id.layoutCompassToggle);
		WP_info = (WayPointInfoControl) findViewById(R.id.WaypointDescriptionView);
		AlignButton = (MultiToggleButton) findViewById(R.id.CompassAlignButton);
		align = !Config.settings.CompassNorthOriented.getValue();
		if (!align) AlignButton.setState(AlignButton.getState() + 1);
		AlignButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				AlignButton.onClick(arg0);
				align = (AlignButton.getState() == 0) ? false : true;

			}
		});

		DescriptionTextView.setOnClickListener(onDescClick);
		WP_info.setOnClickListener(onDescClick);

		SelectedLangChangedEventCalled();

		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());

	}

	final View.OnClickListener onDescClick = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{// bei click auf WP zeige WayPointView
			((main) main.mainActivity).showView(ViewConst.WAYPOINT_VIEW);
			Toast.makeText(main.mainActivity, "Switch to Waypoint View", Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// we overriding onMeasure because this is where the application gets
		// its right size.
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		DescriptionTextView.setHeight(UiSizes.getCacheInfoHeight());
		ToggleButtonLayout.getLayoutParams().height = UiSizes.getWindowWidth() + 30 - ((int) QuickButtonList.that.getHeight());

	}

	public void reInit()
	{
		compassControl.init();
		this.setBackgroundColor(Global.getColor(R.attr.myBackground));
		SetSelectedCache(aktCache, aktWaypoint);
		int cacheInfoBackColor = Global.getColor(R.attr.ListBackground_select);
		if (aktWaypoint != null)
		{
			cacheInfoBackColor = Global.getColor(R.attr.ListBackground_secend);
		}
		DescriptionTextView.setCache(aktCache, cacheInfoBackColor);
		DescriptionTextView.invalidate();
		WP_info.invalidate();
	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (WP_info == null) return;

		if ((aktCache != cache) || (aktWaypoint != waypoint))
		{
			aktCache = cache;
			aktWaypoint = waypoint;

			int cacheInfoBackColor = Global.getColor(R.attr.ListBackground_select);
			if (aktWaypoint != null)
			{
				cacheInfoBackColor = Global.getColor(R.attr.ListBackground_secend); // Cache
																					// ist
																					// nicht
																					// selectiert
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
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
	}

	@Override
	public void OnShow()
	{
		compassControl.N = Config.settings.nightMode.getValue();
		PositionEventList.Add(this);
		SelectedLangChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);
	}

	@Override
	public void OnHide()
	{
		PositionEventList.Remove(this);
		SelectedLangChangedEventList.Remove(this);
		SelectedCacheEventList.Remove(this);
	}

	@Override
	public void OnFree()
	{
		PositionEventList.Remove(this);
		SelectedLangChangedEventList.Remove(this);
		aktCache = null;
		aktWaypoint = null;
		compassControl.dispose();
		compassControl = null;
		DescriptionTextView = null;
		ToggleButtonLayout = null;
		AlignButton = null;
		WP_info = null;

	}

	@Override
	public int GetMenuId()
	{

		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

	@Override
	public void PositionChanged(Location location)
	{
		if (aktCache == null) return;

		if (GlobalCore.LastValidPosition.Valid)
		{
			Coordinate position = GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;
			if (!align) heading = 0;

			Coordinate dest = aktCache.Pos;
			float distance = aktCache.Distance(false);
			if (aktWaypoint != null)
			{
				dest = aktWaypoint.Pos;
				distance = aktWaypoint.Distance();
			}
			double bearing = Coordinate.Bearing(position, dest);
			double relativeBearing = bearing - heading;

			compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(distance));

		}
	}

	@Override
	public void OrientationChanged(float Testheading)
	{
		if (aktCache == null) return;

		if (GlobalCore.LastValidPosition.Valid)
		{
			Coordinate position = GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;
			if (!align) heading = 0;
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
			// double relativeBearingRad = relativeBearing * Math.PI / 180.0;

			compassControl.setInfo(heading, relativeBearing, UnitFormatter.DistanceString(distance));

		}
	}

	@Override
	public void SelectedLangChangedEventCalled()
	{
		if (AlignButton == null) return;
		AlignButton.clearStates();
		AlignButton.addState(GlobalCore.Translations.Get("Align"), Color.GRAY);
		AlignButton.addState(GlobalCore.Translations.Get("Align"), Color.GREEN);
		AlignButton.setState(align ? 1 : 0);
	}

	@Override
	public int GetContextMenuId()
	{

		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{

		return false;
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		SetSelectedCache(cache, waypoint);
	}

	@Override
	public String getReceiverName()
	{
		return "CompassView";
	}

}
