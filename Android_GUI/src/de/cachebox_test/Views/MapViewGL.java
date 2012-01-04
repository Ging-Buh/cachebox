package de.cachebox_test.Views;

import CB_Core.Config;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Map.MapViewGlListener;
import de.cachebox_test.Ui.AllContextMenuCallHandler;

public class MapViewGL extends RelativeLayout implements ViewOptionsMenu
{
	public MapViewGlListener mapViewGlListener;

	public MapViewGL(Context context, LayoutInflater inflater, View glView, MapViewGlListener mapViewGlListener)
	{
		super(context);
		this.mapViewGlListener = mapViewGlListener;
		try
		{

			RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapviewgl, null, false);
			this.addView(mapviewLayout);

			mapviewLayout.addView(glView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
		catch (Exception ex)
		{
			int i = 0;
			i++;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			// SetCurrentLayer(MapView.Manager.GetLayerByName(item.getTitle().toString(), item.getTitle().toString(), ""));
			return true;

		case R.id.mi_Track:
			AllContextMenuCallHandler.showTrackContextMenu();
			return true;

		case R.id.mapview_smooth:
			AllContextMenuCallHandler.showMapSmoothMenu();
			return true;

		case R.id.layer:
			AllContextMenuCallHandler.showMapLayerMenu();
			return true;

		case R.id.mimapview_view:
			AllContextMenuCallHandler.showMapViewGLLayerMenu();
			return true;

		case R.id.miAlignCompass:
			mapViewGlListener.alignToCompass = !mapViewGlListener.alignToCompass;
			return true;

			// case R.id.mapview_smooth_none:
			// setSmotthScrolling(SmoothScrollingTyp.none);
			// return true;
			// case R.id.mapview_smooth_normal:
			// setSmotthScrolling(SmoothScrollingTyp.normal);
			// return true;
			// case R.id.mapview_smooth_fine:
			// setSmotthScrolling(SmoothScrollingTyp.fine);
			// return true;
			// case R.id.mapview_smooth_superfine:
			// setSmotthScrolling(SmoothScrollingTyp.superfine);
			// return true;

		case R.id.mapview_go_settings:
			final Intent mainIntent = new Intent().setClass(main.mainActivity,
					de.cachebox_test.Views.AdvancedSettingsForms.SettingsScrollView.class);
			Bundle b = new Bundle();
			b.putSerializable("Show", 3); // Show Settings und setze ein
											// PerformClick auf den MapSettings
											// Button! (3)
			mainIntent.putExtras(b);
			main.mainActivity.startActivity(mainIntent);
			return true;
		case R.id.miMap_HideFinds:
			mapViewGlListener.hideMyFinds = !mapViewGlListener.hideMyFinds;
			Config.settings.MapHideMyFinds.setValue(mapViewGlListener.hideMyFinds);
			Config.AcceptChanges();

			return true;

		case R.id.miMap_ShowDT:
			mapViewGlListener.showDT = !mapViewGlListener.showDT;
			Config.settings.MapShowDT.setValue(mapViewGlListener.showDT);
			Config.AcceptChanges();
			return true;

		case R.id.miMap_ShowRatings:
			mapViewGlListener.showRating = !mapViewGlListener.showRating;
			Config.settings.MapShowRating.setValue(mapViewGlListener.showRating);
			Config.AcceptChanges();
			return true;

		case R.id.miMap_ShowTitles:
			mapViewGlListener.showTitles = !mapViewGlListener.showTitles;
			Config.settings.MapShowTitles.setValue(mapViewGlListener.showTitles);
			Config.AcceptChanges();
			return true;

		case R.id.miMap_ShowDirektLine:
			mapViewGlListener.showDirektLine = !mapViewGlListener.showDirektLine;
			Config.settings.ShowDirektLine.setValue(mapViewGlListener.showDirektLine);
			Config.AcceptChanges();
			return true;

		}
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		AllContextMenuCallHandler.showMapViewGLContextMenu();
	}

	@Override
	public int GetMenuId()
	{
		return 0;
	}

	@Override
	public void OnShow()
	{
		mapViewGlListener.onStart();
	}

	@Override
	public void OnHide()
	{
		mapViewGlListener.onStop();
	}

	@Override
	public void OnFree()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub

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

	public void Initialize()
	{
		mapViewGlListener.Initialize();
	}

	public void InitializeMap()
	{
		mapViewGlListener.InitializeMap();

	}

}
