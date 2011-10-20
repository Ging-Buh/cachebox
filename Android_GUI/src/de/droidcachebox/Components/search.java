/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.droidcachebox.Components;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.FilterSettings.EditFilterSettings;
import de.droidcachebox.Views.FilterSettings.FilterSetListView;
import de.droidcachebox.Views.FilterSettings.FilterSetListViewItem;
import de.droidcachebox.Views.FilterSettings.PresetListView;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.PleaseWaitMessageBox;

/**
 * Enthält die Methoden für die Suche in der DB oder über die API
 * 
 * @author Longri
 */
public class search
{

	/**
	 * True, wenn der Searchdialog sichtbar ist.
	 */
	private boolean mIsVisible = true;

	/**
	 * Pointer zu main, wird mit dem Constructor übergeben
	 */
	private main mPtrMain = null;

	/**
	 * True, wenn eine Suche läuft und der Iterator mit Next weiter durchlaufen
	 * werden kann.
	 */
	private boolean mSearchAktive = false;

	/**
	 * True, wenn die QuickButtonList beim öffnen dieses Dialogs aufgeklappt
	 * war.
	 */
	private boolean mQuickButtonListWasShow;

	/*
	 * Buttons
	 */

	/**
	 * Option Title, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnTitle;

	/**
	 * Option GC-Code, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnGc;

	/**
	 * Option Owner, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnOwner;

	/**
	 * Option Online Suche On/Off
	 */
	private MultiToggleButton mTglBtnOnline;

	/**
	 * Button, welcher eine Suchanfrage als Filter verwendet
	 */
	private Button mBtnFilter;

	/**
	 * Button, der eine Suche Startet
	 */
	private Button mBtnSearch;

	/**
	 * Button, der den nächsten Treffer einer gestarteten Suche findet
	 */
	private Button mBtnNext;

	/**
	 * Button, der den Search Dialog schliesst
	 */
	private Button mBtnCancel;

	/**
	 * Such Eingabe Feld
	 */
	private EditText mEingabe;

	/**
	 * enthält den Index des element der CacheListe, an der die Suche steht
	 */
	private int mSearchIndex = -1;

	/**
	 * Enthält einen Iterator der aktuell durschten CacheList
	 */
	private Iterator<Cache> CacheListIterator = null;

	/**
	 * Enthällt den Aktuellen Such Modus <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private int mSearchState = 0;

	/**
	 * Constructor mit Übergabe der main Class zur initalisierung des Dialogs
	 * mit all seinen Buttons
	 * 
	 * @param Main
	 */
	public search(main Main)
	{
		mPtrMain = Main;

		// initial Buttons
		mTglBtnTitle = (MultiToggleButton) mPtrMain.findViewById(R.id.search_title);
		mTglBtnGc = (MultiToggleButton) mPtrMain.findViewById(R.id.search_gc);
		mTglBtnOwner = (MultiToggleButton) mPtrMain.findViewById(R.id.search_owner);
		mTglBtnOnline = (MultiToggleButton) mPtrMain.findViewById(R.id.search_on);

		mBtnFilter = (Button) mPtrMain.findViewById(R.id.search_filter);
		mBtnSearch = (Button) mPtrMain.findViewById(R.id.search_search);
		mBtnNext = (Button) mPtrMain.findViewById(R.id.search_next);
		mBtnCancel = (Button) mPtrMain.findViewById(R.id.search_cancel);

		mEingabe = (EditText) mPtrMain.findViewById(R.id.search_edit);

		mEingabe.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0)
			{
				textBox_TextChanged();

			}
		});

		mEingabe.setOnFocusChangeListener(new OnFocusChangeListener()
		{

			@Override
			public void onFocusChange(View arg0, boolean arg1)
			{
				// wenn das Eingabe Feld den Focus verliert,
				// kann die Tastertur wieder zu!

				if (!arg1) // if lost the Focus
				{
					closeSoftKeyPad();
				}

			}
		});

		mEingabe.setText("");

		setLang();
		switchSearcheMode(0);

		mBtnCancel.setOnClickListener(cancelButtonOnClick);

		mTglBtnTitle.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				switchSearcheMode(0);

			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				switchSearcheMode(1);

			}
		});

		mTglBtnOwner.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				switchSearcheMode(2);

			}
		});

		mBtnSearch.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				closeSoftKeyPad();
				mSearchAktive = false;
				mSearchIndex = -1;
				searchNow(false);
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				closeSoftKeyPad();
				searchNow(true);

			}
		});

		mBtnFilter.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				closeSoftKeyPad();
				if (mTglBtnOnline.getState() == 1)
				{
					((main)main.mainActivity).searchOnline();
					Hide();
				}
				else
				{
					setFilter();
				}
				
			}
		});

		mTglBtnOnline.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				if (mTglBtnOnline.getState() == 1)
				{
					mTglBtnOnline.setState(0);
					mBtnFilter.setCompoundDrawables(null, null, null, null);
					mBtnFilter.setText(Global.Translations.Get("Filter"));
				}
				else
				{
					mTglBtnOnline.setState(1);
					mBtnFilter.setCompoundDrawablesWithIntrinsicBounds(Global.Icons[43], null, null, null);

					mBtnFilter.setText("");
				}
				textBox_TextChanged();
			}
		});

	}

	/**
	 * Blendet den search Dialog ein
	 */
	public void Show()
	{

		setBackGroundDrawable();

		// set Visibility to VISIBLE nur in CacheListView und MapView
		mPtrMain.searchLayout.setVisibility(View.VISIBLE);
		mIsVisible = true;
		setLang();

		// wenn QuickButtonList ausgeklappt, dann schliesse sie und öffene sie
		// erst wieder mit dem
		// Schliessen dieses Dialogs
		mQuickButtonListWasShow = Config.GetBool("quickButtonLastShow") && Config.GetBool("quickButtonShow");
		mPtrMain.setQuickButtonHeight(0);
		Config.Set("quickButtonShow", false);
		Config.AcceptChanges();

		// initalisier mit Title Suche
		switchSearcheMode(0);

		mEingabe.setText("");
	}

	/**
	 * Blendet den search Dialog aus
	 */
	public void Hide()
	{
		// set Visibility to GONE
		mPtrMain.searchLayout.setVisibility(View.GONE);
		mIsVisible = false;
		mSearchAktive = false;

		// QuickButtonList wieder einblenden, wenn sie von diesem
		// Dialog ausgeblendet wurden
		if (mQuickButtonListWasShow)
		{
			Config.Set("quickButtonShow", true);
			Config.AcceptChanges();
			mPtrMain.setQuickButtonHeight(Sizes.getQuickButtonListHeight());
		}
	}

	/**
	 * Lifert True, wenn der Search Dialog eingeblendet ist. Andernfalls False;
	 * 
	 * @return boolean
	 */
	public boolean isVisible()
	{
		return mIsVisible;
	}

	/**
	 * Setzt die Übersetzten Texte auf die Buttons
	 */
	private void setLang()
	{
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Global.Translations.Get("Title"), Global.Translations.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Global.Translations.Get("GCCode"), Global.Translations.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Global.Translations.Get("Owner"), Global.Translations.Get("Owner"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOnline, "Online", "Online");

		// der State muss erstmal gesetzt werden, damit die Anzeige
		// Aktuallisiert wird
		mTglBtnOnline.setState(0);

		mBtnFilter.setText(Global.Translations.Get("Filter"));
		mBtnSearch.setText(Global.Translations.Get("Search"));
		mBtnNext.setText(Global.Translations.Get("Next"));
		mBtnCancel.setText(Global.Translations.Get("abort"));

	}

	/**
	 * Setzt die Hintergrund Images des Dialogs in Abhängigkeit vom Theme
	 */
	private void setBackGroundDrawable()
	{
		Resources res = mPtrMain.getResources();

		Drawable header = res.getDrawable(main.N ? R.drawable.night_header : R.drawable.header);
		// Drawable title = res.getDrawable(main.N? R.drawable.night_title :
		// R.drawable.title);
		Drawable center = res.getDrawable(main.N ? R.drawable.night_center : R.drawable.center);
		Drawable footer = res.getDrawable(main.N ? R.drawable.night_footer : R.drawable.footer);

		((LinearLayout) mPtrMain.findViewById(R.id.header)).setBackgroundDrawable(header);
		// ((TextView)
		// mPtrMain.findViewById(R.id.title)).setBackgroundDrawable(title);
		((LinearLayout) mPtrMain.findViewById(R.id.content)).setBackgroundDrawable(center);
		((RelativeLayout) mPtrMain.findViewById(R.id.footer)).setBackgroundDrawable(footer);
	}

	private OnClickListener cancelButtonOnClick = new OnClickListener()
	{

		@Override
		public void onClick(View arg0)
		{
			Hide();

		}
	};

	/**
	 * Schaltet den Such Modus um.
	 * 
	 * @param state
	 * <br/>
	 *            0 = Title <br/>
	 *            1 = Gc-Code <br/>
	 *            2 = Owner <br/>
	 */
	private void switchSearcheMode(int state)
	{
		mSearchState = state;

		if (state == 0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
		}
		if (state == 1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
		}
		if (state == 2)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(1);
		}

	}

	private void textBox_TextChanged()
	{
		// reset SearchIndex, because of text changed.
		mSearchIndex = -1;

		boolean isText = mEingabe.getEditableText().toString().length() != 0;
		ActivityUtils.setBtnState(mBtnSearch, isText);
		ActivityUtils.setBtnState(mBtnNext, false);

		if (mTglBtnOnline.getState() == 0)
		{
			ActivityUtils.setBtnState(mBtnFilter, isText);
		}
		else
		{
			ActivityUtils.setBtnState(mBtnFilter, true);
		}

	}

	/**
	 * schliesst die virtuelle Tastertur
	 */
	private void closeSoftKeyPad()
	{
		// close the virtual keyboard
		InputMethodManager mgr = (InputMethodManager) mPtrMain.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mEingabe.getWindowToken(), 0);

	}

	/**
	 * Die aktive CahcheList wird durchsucht gefilterte Caches werden dabei
	 * nicht berücksichtigt.
	 * 
	 * @param ignoreOnlineSearch
	 *            (True, wenn Lokal gesucht werden soll, obwohl der
	 *            MultiToggleButton "Online" aktiviert ist.
	 */
	private void searchNow(boolean ignoreOnlineSearch)
	{

		if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0)
		{

			mSearchIndex++;

			String searchPattern = mEingabe.getEditableText().toString().toLowerCase();

			boolean criterionMatches = false;

			if (!mSearchAktive)
			{
				CacheListIterator = Database.Data.Query.iterator();
				mSearchAktive = true;
			}

			Cache tmp = null;
			while (CacheListIterator.hasNext() && !criterionMatches)
			{

				tmp = CacheListIterator.next();

				// criterionMatches = (mSearchState == 0 &&
				// tmp.Name.toLowerCase().contains(searchPattern))
				// || (mSearchState == 1 &&
				// tmp.GcCode.toLowerCase().contains(searchPattern))
				// || (mSearchState == 2 &&
				// tmp.Owner.toLowerCase().contains(searchPattern))
				// || (mSearchState == 2 &&
				// tmp.PlacedBy.toLowerCase().contains(searchPattern));

				switch (mSearchState)
				{
				case 0:
					criterionMatches = tmp.Name.toLowerCase().contains(searchPattern);
					break;
				case 1:
					criterionMatches = tmp.GcCode.toLowerCase().contains(searchPattern);
					break;
				case 2:
					criterionMatches = tmp.Owner.toLowerCase().contains(searchPattern)
							|| tmp.PlacedBy.toLowerCase().contains(searchPattern);
					break;
				}

				if (!criterionMatches) mSearchIndex++;
			}

			if (!criterionMatches)
			{
				ActivityUtils.setBtnState(mBtnNext, false);
				mSearchAktive = false;
				MessageBox.Show(Global.Translations.Get("NoCacheFound"), Global.Translations.Get("search"), MessageBoxButtons.OK,
						MessageBoxIcon.Asterisk, null);
			}
			else
			{

				Waypoint finalWp = null;
				if (tmp.HasFinalWaypoint()) finalWp = tmp.GetFinalWaypoint();
				if (tmp != null) GlobalCore.SelectedWaypoint(tmp, finalWp);
				// deactivate autoResort when Cache is selected by hand
				Global.autoResort = false;

				ActivityUtils.setBtnState(mBtnNext, true);
			}
		}
		else
		{
			searchAPI();
		}

	}

	/**
	 * Sucht mit den Vorgaben nach Caches über die API. Die Gefundenen Caches
	 * werden in die DB eingetragen und im Anschluss wird der lokale Suchvorgang
	 * gestartet.
	 */
	private void searchAPI()
	{
		searchOnline();
	}

	/**
	 * setzt den Filter auf die Such Anfrage
	 */
	private void setFilter()
	{
		String searchPattern = mEingabe.getEditableText().toString().toLowerCase();

		String where = "";
		if (Global.LastFilter.toString().length() > 0) where = " AND (";

		Global.LastFilter.filterName = "";
		Global.LastFilter.filterGcCode = "";
		Global.LastFilter.filterOwner = "";

		if (mSearchState == 0) Global.LastFilter.filterName = searchPattern;
		else if (mSearchState == 1) Global.LastFilter.filterGcCode = searchPattern;
		if (mSearchState == 2) Global.LastFilter.filterOwner = searchPattern;

		ApplyFilter();
	}

	// private android.app.ProgressDialog pd;
	private FilterProperties props;

	public void ApplyFilter()
	{
		ApplyFilter(Global.LastFilter);
	}

	public void ApplyFilter(FilterProperties filter)
	{

		props = filter;
		// pd = android.app.ProgressDialog.show(mPtrMain, "",
		// Global.Translations.Get("LoadCaches"), true);
		pd = PleaseWaitMessageBox.Show(Global.Translations.Get("LoadCaches"), "Groundspeak API", MessageBoxButtons.NOTHING, null);

		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				String sqlWhere = props.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				Database.Data.Query.clear();
				CacheListDAO cacheListDAO = new CacheListDAO();
				cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
				messageHandler.sendMessage(messageHandler.obtainMessage(1));
			}

		};

		thread.start();
	}

	private boolean reSearch = false;

	private Handler messageHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				CachListChangedEventList.Call();
				pd.dismiss();
				Toast.makeText(
						main.mainActivity,
						Global.Translations.Get("AppliedFilter1") + " " + String.valueOf(Database.Data.Query.size()) + " "
								+ Global.Translations.Get("AppliedFilter2"), Toast.LENGTH_LONG).show();

				if (reSearch)
				{
					reSearch = false;
					searchNow(true);
				}
			}

			}

		}

	};

	// ''''''''''''' Online Search

	public void searchOnline()
	{
		IsPremiumThread = new isPremiumThread();
		IsPremiumThread.execute("");
		pd = PleaseWaitMessageBox.Show("Chk API State", "Groundspeak API", MessageBoxButtons.Cancel, MessageBoxIcon.Powerd_by_GC_Live,
				Cancel1ClickListner);
	}

	private isPremiumThread IsPremiumThread;

	private final DialogInterface.OnClickListener Cancel1ClickListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			IsPremiumThread.cancel(true);
			dialog.dismiss();
		}
	};

	private class isPremiumThread extends AsyncTask<String, Integer, Integer>
	{
		private boolean canceld = false;

		@Override
		protected void onCancelled()
		{
			canceld = true;
		}

		@Override
		protected Integer doInBackground(String... params)
		{
			int ret = GroundspeakAPI.GetMembershipType(Config.GetAccessToken());
			if (canceld)
			{
				isPremiumReadyHandler.sendMessage(isPremiumReadyHandler.obtainMessage(10));
				return null;
			}
			isPremiumReadyHandler.sendMessage(isPremiumReadyHandler.obtainMessage(ret));
			return null;
		}

	}

	private Handler isPremiumReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			pd.dismiss();

			if (msg.what == 10)
			{
				pd.dismiss();
				// cancel
			}
			else if (msg.what == 3)
			{
				searchOnlineNow();
			}
			else if (msg.what == -1)
			{
				pd.dismiss();
				MessageBox.Show("at online search", "Error", MessageBoxIcon.Error);

			}
			else
			{
				MessageBox.Show(Global.Translations.Get("GC_basic"), Global.Translations.Get("GC_title"), MessageBoxButtons.OKCancel,
						MessageBoxIcon.Powerd_by_GC_Live, PremiumMemberResult);
			}
		}
	};

	private DialogInterface.OnClickListener PremiumMemberResult = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			switch (button)
			{
			case -1:
				searchOnlineNow();
				break;

			}

			dialog.dismiss();

		}
	};

	private DialogInterface pd;
	private loaderThread LoaderThread;

	private void searchOnlineNow()
	{
		LoaderThread = new loaderThread();
		LoaderThread.execute("");
		pd = PleaseWaitMessageBox.Show("Search Online", "Groundspeak API", MessageBoxButtons.Cancel, MessageBoxIcon.Powerd_by_GC_Live,
				CancelClickListner);
	}

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

		private boolean canceld = false;

		@Override
		protected void onCancelled()
		{
			canceld = true;
		}

		@Override
		protected Integer doInBackground(String... params)
		{
			String accessToken = Config.GetAccessToken();

			Coordinate searchCoord = null;

			if (main.mapView.isShown())
			{
				PointD point = new PointD(0, 0);
				point.X = main.mapView.screenCenter.X;
				point.Y = main.mapView.screenCenter.Y;
				main.mapView.lastMouseCoordinate = new Coordinate(Descriptor.TileYToLatitude(main.mapView.Zoom, point.Y / (256.0)),
						Descriptor.TileXToLongitude(main.mapView.Zoom, point.X / (256.0)));
				searchCoord = main.mapView.lastMouseCoordinate;
			}
			else
			{
				searchCoord = GlobalCore.LastValidPosition;
			}

			if (searchCoord == null)
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
				return null;
			}

			// alle per API importierten Caches landen in der Category und
			// GpxFilename
			// API-Import
			// Category suchen, die dazu gehört
			CategoryDAO categoryDAO = new CategoryDAO();
			Category category = categoryDAO.GetCategory(GlobalCore.Categories, "API-Import");
			if (category == null) return null; // should not happen!!!

			GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
			if (gpxFilename == null) return null;

			ArrayList<Cache> apiCaches = new ArrayList<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();

			CB_Core.Api.SearchForGeocaches.Search searchC = null;

			String searchPattern = mEingabe.getEditableText().toString().toLowerCase();

			if (canceld)
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(10));
				return null;
			}

			// * 0 = Title <br/>
			// * 1 = Gc-Code <br/>
			// * 2 = Owner <br/>

			switch (mSearchState)
			{
			case 0:
				CB_Core.Api.SearchForGeocaches.SearchGCName searchCName = new CB_Core.Api.SearchForGeocaches.SearchGCName();
				searchCName.pos = searchCoord;
				searchCName.distanceInMeters = 5000000;
				searchCName.number = 50;
				searchCName.gcName = searchPattern;
				searchC = searchCName;
				break;

			case 1:
				CB_Core.Api.SearchForGeocaches.SearchGC searchCGC = new CB_Core.Api.SearchForGeocaches.SearchGC();
				searchCGC.gcCode = searchPattern;
				searchCGC.number = 1;
				searchC = searchCGC;
				break;

			case 2:
				CB_Core.Api.SearchForGeocaches.SearchGCOwner searchCOwner = new CB_Core.Api.SearchForGeocaches.SearchGCOwner();
				searchCOwner.OwnerName = searchPattern;
				searchCOwner.number = 50;
				searchCOwner.pos = searchCoord;
				searchCOwner.distanceInMeters = 5000000;

				searchC = searchCOwner;
				break;
			}

			if (searchC == null)
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
				return null;
			}

			CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, gpxFilename.Id);

			if (canceld)
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(10));
				return null;
			}
			if (apiCaches.size() > 0)
			{
				Database.Data.beginTransaction();
				for (Cache cache : apiCaches)
				{
					if (canceld)
					{
						onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(10));
						return null;
					}
					cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
					cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());
					if (Database.Data.Query.GetCacheById(cache.Id) == null)
					{
						Database.Data.Query.add(cache);
						CacheDAO cacheDAO = new CacheDAO();
						cacheDAO.WriteToDatabase(cache);
						for (LogEntry log : apiLogs)
						{
							if (log.CacheId != cache.Id) continue;
							// Write Log to database
							LogDAO logDAO = new LogDAO();
							logDAO.WriteToDatabase(log);
						}
						for (Waypoint waypoint : cache.waypoints)
						{
							WaypointDAO waypointDAO = new WaypointDAO();
							waypointDAO.WriteToDatabase(waypoint);
						}
					}
				}
				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();

				Database.Data.GPXFilenameUpdateCacheCount();

				if (main.mapView.isShown())
				{
					main.mapView.updateCacheList();
					main.mapView.Render(true);
				}

			}

			onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
			return null;
		}

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
				CachListChangedEventList.Call();
				main.cacheListView.notifyCacheListChange();
				// Lokale Suche ausführen!
				// Und dabei den Filter zurück setzen.
				reSearch = true;
				ApplyFilter(new FilterProperties(PresetListView.presets[0]));

				break;
			}

			case 2:
			{
				pd.dismiss();
				MessageBox.Show("at online search", "Error", MessageBoxIcon.Error);
				break;
			}
			case 10:
			{
				pd.dismiss();
				// Cancel
				break;
			}
			}
		}
	};

}
