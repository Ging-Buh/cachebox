package CB_Core.GL_UI.Views;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class LogView extends V_ListView
{
	public static CB_RectF ItemRec;
	public static LogView that;

	public LogView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		ItemRec = new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() * 1.1f);
		setBackground(SpriteCache.ListBack);

		mustLoad = false;

		this.setBaseAdapter(null);
		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());

	}

	@Override
	public void onShow()
	{
		if (mustLoad)
		{
			this.setBaseAdapter(null);
			lvAdapter = new CustomAdapter(aktCache);
			this.setBaseAdapter(lvAdapter);
			this.notifyDataSetChanged();
			mustLoad = false;
		}
	}

	@Override
	public void onHide()
	{

	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	Cache aktCache;
	boolean mustLoad;
	CustomAdapter lvAdapter;

	public class CustomAdapter implements Adapter
	{
		private Cache cache;
		private ArrayList<LogEntry> logs;

		public CustomAdapter(Cache cache)
		{
			this.cache = cache;
			ArrayList<LogEntry> cleanLogs = new ArrayList<LogEntry>();
			cleanLogs = Database.Logs(cache);// cache.Logs();

			// clean up logs
			logs = new ArrayList<LogEntry>();
			for (LogEntry l : cleanLogs)
			{
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

		@Override
		public ListViewItemBase getView(int position)
		{
			if (cache != null)
			{
				LogEntry logEntry = logs.get(position);

				CB_RectF rec = ItemRec.copy().ScaleCenter(0.97f);
				rec.setHeight(mesureItemHeight(logEntry));
				LogViewItem v = new LogViewItem(rec, position, logEntry);

				return v;

			}
			else
				return null;
		}

		@Override
		public float getItemSize(int position)
		{
			if (position > logs.size() || logs.size() == 0) return 0;
			LogEntry logEntry = logs.get(position);
			return mesureItemHeight(logEntry);
		}

		private float mesureItemHeight(LogEntry logEntry)
		{
			float headHeight = (UiSizes.getButtonHeight() / 1.5f) + (Dialog.margin);
			float commentHeight = (Dialog.margin * 2)
					+ Fonts.MesureWrapped(logEntry.Comment, ItemRec.getWidth() - (Dialog.margin * 2)).height;

			return headHeight + commentHeight;
		}
	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			aktCache = cache;
			mustLoad = true;
		}
	}

}
