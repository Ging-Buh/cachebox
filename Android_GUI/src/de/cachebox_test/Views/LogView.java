package de.cachebox_test.Views;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;

public class LogView extends ListView implements ViewOptionsMenu
{

	Cache aktCache;
	boolean mustLoad;
	CustomAdapter lvAdapter;

	/**
	 * Constructor
	 */
	public LogView(Context context)
	{
		super(context);
		mustLoad = false;

		this.setAdapter(null);

		this.setLongClickable(true);
		this.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				/*
				 * Waypoint aktWaypoint = null; if (arg2 > 0) aktWaypoint = Global.SelectedCache().waypoints.get(arg2 - 1);
				 * Global.SelectedWaypoint(Global.SelectedCache(), aktWaypoint);
				 */
				return true;
			}
		});
		ActivityUtils.setListViewPropertys(this);
		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
	}

	public class CustomAdapter extends BaseAdapter /*
													 * implements OnClickListener
													 */
	{

		/*
		 * private class OnItemClickListener implements OnClickListener{ private int mPosition; OnItemClickListener(int position){ mPosition
		 * = position; } public void onClick(View arg0) { Log.v("ddd", "onItemClick at position" + mPosition); } }
		 */

		private Context context;
		private Cache cache;
		private ArrayList<LogEntry> logs;

		public CustomAdapter(Context context, Cache cache)
		{

			this.context = context;
			this.cache = cache;
			ArrayList<LogEntry> cleanLogs = new ArrayList<LogEntry>();
			cleanLogs = Database.Logs(cache);// cache.Logs();

			// clean up logs
			logs = new ArrayList<LogEntry>();
			for (LogEntry l : cleanLogs)
			{
				// if (l.TypeIcon != -1)
				logs.add(l);
			}

		}

		public void setCache(Cache cache)
		{
			this.cache = cache;

		}

		public int getCount()
		{
			if (cache != null)
			{
				return logs.size();
			}
			else
			{
				return 0;
			}
		}

		public Object getItem(int position)
		{
			if (cache != null)
			{
				return logs.get(position);
			}
			else
				return null;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (cache != null)
			{
				LogEntry logEntry = logs.get(position);
				Boolean BackGroundChanger = ((position % 2) == 1);
				LogViewItem v = new LogViewItem(context, cache, logEntry, BackGroundChanger);
				return v;
			}
			else
				return null;
		}

	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			aktCache = cache;
			mustLoad = true;
		}
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
	}

	@Override
	public void OnShow()
	{
		// reinitial colors
		ActivityUtils.setListViewPropertys(this);
		LogViewItem.Linepaint = null;
		LogViewItem.NamePaint = null;
		LogViewItem.textPaint = null;

		if (mustLoad)
		{
			this.setAdapter(null);
			lvAdapter = new CustomAdapter(getContext(), aktCache);
			this.setAdapter(lvAdapter);
			lvAdapter.notifyDataSetChanged();
			mustLoad = false;
		}
	}

	@Override
	public void OnHide()
	{
	}

	@Override
	public void OnFree()
	{
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

}
