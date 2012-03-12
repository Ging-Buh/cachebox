package de.cachebox_test.Views;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.TrackableListDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.LogEntry;
import CB_Core.Types.TbList;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import de.cachebox_test.R;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;

/**
 * Liste zur darstellung und verwaltung der Routes in RouteOverlay.Routes <br>
 * <br>
 * <br>
 * <img src="doc-files/TrackListView.png" width=250 height=400>
 * 
 * @author Longri
 */
public class TrackableListView extends ListView implements ViewOptionsMenu
{

	CustomAdapter lvAdapter;
	Activity parentActivity;
	protected static float lastTouchX;
	protected static float lastTouchY;
	TrackableListViewItem selectedItem;
	int[] ColorField = new int[]
		{ Color.RED, Color.YELLOW, Color.BLACK, Color.GREEN, Color.GRAY };

	TbList trackableList = null;

	/**
	 * Constructor
	 */
	public TrackableListView(final Context context, final Activity parentActivity)
	{
		super(context);
		this.parentActivity = parentActivity;
		this.setAdapter(null);

		trackableList = TrackableListDAO.ReadTbList("");

		lvAdapter = new CustomAdapter(getContext());
		this.setAdapter(lvAdapter);
		this.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{

				Rect HitRec = new Rect();
				arg1.getHitRect(HitRec);
				Rect colorChangeHitRec = new Rect(HitRec.left, HitRec.top, HitRec.left + HitRec.height(), HitRec.bottom);
				Rect chkBoxHitRec = new Rect(HitRec.width() - HitRec.height(), HitRec.top, HitRec.right, HitRec.bottom);

				if (chkBoxHitRec.contains((int) TrackableListView.lastTouchX, (int) TrackableListView.lastTouchY))
				{

					lvAdapter.notifyDataSetInvalidated();
					invalidate();
				}
				else if (colorChangeHitRec.contains((int) TrackableListView.lastTouchX, (int) TrackableListView.lastTouchY))
				{

				}
				else
				{
					((TrackableListViewItem) arg1).setSelected(true);
					selectedItem = ((TrackableListViewItem) arg1);
				}

				return;
			}

		});

		this.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1)
			{
				lastTouchX = arg1.getX();
				lastTouchY = arg1.getY();
				return false;
			}
		});

		ActivityUtils.setListViewPropertys(this);

	}

	static public int windowW = 0;
	static public int windowH = 0;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// we overriding onMeasure because this is where the application gets
		// its right size.
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		windowW = getMeasuredWidth();
		windowH = getMeasuredHeight();
	}

	public class CustomAdapter extends BaseAdapter /*
													 * implements
													 * OnClickListener
													 */
	{

		private Context context;

		public CustomAdapter(Context context)
		{
			this.context = context;

		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{

			if (trackableList == null || trackableList.size() == 0) return null;

			Boolean BackGroundChanger = ((position % 2) == 1);
			TrackableListViewItem v = new TrackableListViewItem(context, trackableList.get(position), BackGroundChanger);
			return v;
		}

		@Override
		public int getCount()
		{
			if (trackableList == null) return 0;
			return trackableList.size();
		}

		@Override
		public Object getItem(int position)
		{
			if (trackableList != null)
			{
				return trackableList.get(position);
			}
			else
				return null;
		}

	}

	@Override
	public void OnShow()
	{
		ActivityUtils.setListViewPropertys(this);
		selectedItem = null;
		lvAdapter.notifyDataSetInvalidated();
		invalidate();
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

	public boolean ItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case R.id.menu_tracklistview_loadMy:
			LoadInventoryOverApi();
			break;

		}
		return true;
	}

	private DialogInterface pd;
	private loaderThread LoaderThread;

	private void LoadInventoryOverApi()
	{
		LoaderThread = new loaderThread();
		LoaderThread.execute("");
		pd = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("loadingTrackables"), "Groundspeak API", MessageBoxButtons.Cancel,
				MessageBoxIcon.GC_Live, CancelClickListner);
	}

	private Handler onlineSearchReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				pd.dismiss();
				trackableList = TrackableListDAO.ReadTbList("");
				lvAdapter.notifyDataSetChanged();
				break;
			}

			case 2:
			{
				pd.dismiss();
				MessageBox.Show(GlobalCore.Translations.Get("errorAPI"), GlobalCore.Translations.Get("Error"), MessageBoxIcon.Error);
				break;
			}

			}
		}
	};

	private final DialogInterface.OnClickListener CancelClickListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			LoaderThread.cancel(true);
			dialog.dismiss();
		}
	};

	private class loaderThread extends AsyncTask<String, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(String... params)
		{
			TbList searchList = new TbList();
			int result = CB_Core.Api.GroundspeakAPI.getMyTbList(Config.GetAccessToken(), searchList);

			searchList.writeToDB();

			onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(result == 0 ? 1 : 2));
			return null;
		}

	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		AllContextMenuCallHandler.showTrackableListViewContextMenu();
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub

	}

}
