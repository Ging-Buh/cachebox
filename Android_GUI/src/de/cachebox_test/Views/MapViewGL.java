package de.cachebox_test.Views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import de.cachebox_test.R;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Map.MapViewGlListener;

public class MapViewGL extends RelativeLayout implements ViewOptionsMenu
{
	private MapViewGlListener mapViewGlListener;

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
		case R.id.mi_mvgl_AlignCompass:
			mapViewGlListener.SetAlignToCompass(!mapViewGlListener.GetAlignToCompass());
			return true;
		case R.id.mi_mvgl_centergps:
			mapViewGlListener.SetCenterGps(!mapViewGlListener.GetCenterGps());
			return true;
		}
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int GetMenuId()
	{
		return R.menu.menu_mapviewgl;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub
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
