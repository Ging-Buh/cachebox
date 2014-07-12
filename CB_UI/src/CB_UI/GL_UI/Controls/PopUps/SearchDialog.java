/* 
 * Copyright (C) 2014 team-cachebox.de
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
package CB_UI.GL_UI.Controls.PopUps;

import java.util.ArrayList;

import CB_Core.CoreSettingsForward;
import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.Search;
import CB_Core.Api.SearchGC;
import CB_Core.Api.SearchGCName;
import CB_Core.Api.SearchGCOwner;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.IChkRedyHandler;
import CB_UI.GL_UI.Activitys.SearchOverPosition;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.Slider;
import CB_UI.GL_UI.Controls.Slider.YPositionChanged;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Logger;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * @author Longri
 */
public class SearchDialog extends PopUp_Base
{
	public static SearchDialog that;

	/**
	 * True, wenn eine Suche läuft und der Iterator mit Next weiter durchlaufen werden kann.
	 */
	private boolean mSearchAktive = false;

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
	private ImageButton mBtnFilter;

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
	private EditTextField mEingabe;

	/**
	 * Enthällt den Aktuellen Such Modus <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private int mSearchState = 0;

	/**
	 * Index of the beginning search
	 */
	private int beginnSearchIndex = -1;

	public SearchDialog()
	{
		super(new CB_RectF(), "SearchDialog");

		that = this;

		this.setSize(UiSizes.that.getCacheListItemSize().asFloat());

		this.setBackground(SpriteCacheBase.ListBack);

		float margin = UI_Size_Base.that.getMargin();
		if (GlobalCore.isTab) margin *= 2;
		float btnWidth = (this.getWidth() - (margin * 7)) / 4;

		CB_RectF rec = new CB_RectF(0, 0, btnWidth, UI_Size_Base.that.getButtonHeight());

		mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
		mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
		mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");
		mTglBtnOnline = new MultiToggleButton(rec, "mTglBtnOnline");

		rec.setWidth(btnWidth = (this.getWidth() - (margin * 5)) / 4);

		mBtnFilter = new ImageButton(rec, "mBtnFilter");
		mBtnSearch = new Button(rec, "mBtnSearch");
		mBtnNext = new Button(rec, "mBtnNext");
		mBtnCancel = new Button(rec, "mBtnCancel");

		rec.setWidth(this.getWidth() - (margin * 2));

		mEingabe = new EditTextField(this, rec, WrapType.SINGLELINE, "");

		mEingabe.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{

			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{
				textBox_TextChanged();
			}
		});

		mEingabe.setText("");

		// Layout! da es sich nicht ändert, brauchen wir es nicht in eine Methode packen
		float y = margin;

		mBtnFilter.setPos(margin, y);
		mBtnSearch.setPos(mBtnFilter.getMaxX() + margin, y);
		mBtnNext.setPos(mBtnSearch.getMaxX() + margin, y);
		mBtnCancel.setPos(mBtnNext.getMaxX() + margin, y);

		mEingabe.setPos(margin, mBtnCancel.getMaxY() + margin);

		y = mEingabe.getMaxY() + margin;

		mTglBtnOnline.setPos(margin, y);
		mTglBtnTitle.setPos(mTglBtnOnline.getMaxX() + margin + margin, y);
		mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
		mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);

		// die Höhe nach dem Verbrauchten Platz einstellen

		this.setHeight(mTglBtnOwner.getMaxY() + margin);

		// Controls zum Dialog hinzufügen
		this.addChild(mTglBtnTitle);
		this.addChild(mTglBtnGc);
		this.addChild(mTglBtnOwner);
		this.addChild(mTglBtnOnline);
		this.addChild(mBtnFilter);
		this.addChild(mBtnSearch);
		this.addChild(mBtnNext);
		this.addChild(mBtnCancel);
		this.addChild(mEingabe);

		setLang();
		switchSearcheMode(0);

		mBtnCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				close();
				return true;
			}

		});

		mTglBtnTitle.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(0);
				return true;
			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(1);
				return true;
			}
		});

		mTglBtnOwner.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(2);
				return true;
			}
		});

		mBtnSearch.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				mSearchAktive = false;
				beginnSearchIndex = 0;
				searchNow(false);
				return true;
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				searchNow(true);
				return true;

			}
		});

		mBtnFilter.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				if (mTglBtnOnline.getState() == 1)
				{
					close();
					askPremium();
				}
				else
				{
					setFilter();
				}
				return true;
			}
		});

		mTglBtnOnline.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				setFilterBtnState();
				textBox_TextChanged();
				return true;
			}
		});

	}

	private void setFilterBtnState()
	{
		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.clearImage();
			mBtnFilter.setText(Translation.Get("Filter"));
		}
		else
		{

			mBtnFilter.setImage(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.targetDay_53.ordinal())));
			mBtnFilter.setText("");
		}
	}

	/**
	 * Setzt die Übersetzten Texte auf die Buttons
	 */
	private void setLang()
	{
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Translation.Get("Title"), Translation.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Translation.Get("GCCode"), Translation.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Translation.Get("Owner"), Translation.Get("Owner"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOnline, "Online", "Online");

		// der State muss erstmal gesetzt werden, damit die Anzeige
		// Aktuallisiert wird
		mTglBtnOnline.setState(0);

		mBtnFilter.setText(Translation.Get("Filter"));
		mBtnSearch.setText(Translation.Get("Search"));
		mBtnNext.setText(Translation.Get("Next"));
		mBtnCancel.setText(Translation.Get("abort"));

	}

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

		boolean isText = mEingabe.getText().length() != 0;
		mBtnSearch.setEnable(isText);
		mBtnNext.disable();

		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.setEnable(isText);
		}
		else
		{
			mBtnFilter.enable();
		}

		// TODO Sofort Filter hat eine schlechte Performance, deshalb habe ich ihn ersteinmal abgeschalten.
		// Es wäre aber ein schönes Feature!
		// filterSearchByTextChnge();
	}

	/**
	 * schliesst die virtuelle Tastertur
	 */
	private void closeSoftKeyPad()
	{
		// close the virtual keyboard
		// InputMethodManager mgr = (InputMethodManager) mPtrMain.getSystemService(Context.INPUT_METHOD_SERVICE);
		// mgr.hideSoftInputFromWindow(mEingabe.getWindowToken(), 0);
		mEingabe.getOnscreenKeyboard().show(false);
	}

	/**
	 * Die aktive CahcheList wird durchsucht gefilterte Caches werden dabei nicht berücksichtigt.
	 * 
	 * @param ignoreOnlineSearch
	 *            (True, wenn Lokal gesucht werden soll, obwohl der MultiToggleButton "Online" aktiviert ist.
	 */
	private void searchNow(boolean ignoreOnlineSearch)
	{

		if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0)
		{

			// Replase LineBreaks

			String searchPattern = mEingabe.getText().toLowerCase();

			// Replase LineBreaks
			searchPattern = searchPattern.replace("\n", "");
			searchPattern = searchPattern.replace("\r", "");

			boolean criterionMatches = false;

			synchronized (Database.Data.Query)
			{

				if (!mSearchAktive)
				{
					mSearchAktive = true;
				}

				Cache tmp = null;
				for (int i = beginnSearchIndex, n = Database.Data.Query.size(); i < n; i++)
				{
					tmp = Database.Data.Query.get(i);

					switch (mSearchState)
					{
					case 0:
						criterionMatches = tmp.getName().toLowerCase().contains(searchPattern);
						break;
					case 1:
						criterionMatches = tmp.getGcCode().toLowerCase().contains(searchPattern);
						break;
					case 2:
						criterionMatches = tmp.getOwner().toLowerCase().contains(searchPattern);
						break;
					}
					if (criterionMatches)
					{
						beginnSearchIndex = i + 1;
						break;
					}
				}

				if (!criterionMatches)
				{
					mBtnNext.disable();
					mSearchAktive = false;
					GL_MsgBox.Show(Translation.Get("NoCacheFound"), Translation.Get("search"), MessageBoxButtons.OK,
							MessageBoxIcon.Asterisk, null);
				}
				else
				{

					Waypoint finalWp = null;
					if (tmp != null)
					{
						if (tmp.HasFinalWaypoint()) finalWp = tmp.GetFinalWaypoint();
						else if (tmp.HasStartWaypoint()) finalWp = tmp.GetStartWaypoint();
						GlobalCore.setSelectedWaypoint(tmp, finalWp);
					}
					// deactivate autoResort when Cache is selected by hand
					GlobalCore.setAutoResort(false);

					mBtnNext.enable();

				}
			}
		}
		else
		{
			searchAPI();
		}

	}

	// /**
	// * setzt bei Eingabe eines Zeichens die CacheListItems auf Sichtbar oder unsichtbar
	// */
	// private void filterSearchByTextChnge()
	// {
	// if (!Config.settings.dynamicFilterAtSearch.getValue()) return;
	// if (CacheListView.that == null) return;
	// if (mTglBtnOnline.getState() == 1)
	// {
	// // nicht bei Online Suche
	// clearSearchFilter();
	// return;
	// }
	//
	// String searchPattern = mEingabe.getText().toLowerCase();
	//
	// // Replase LineBreaks
	// searchPattern = searchPattern.replace("\n", "");
	// searchPattern = searchPattern.replace("\r", "");
	//
	// synchronized (Database.Data.Query)
	// {
	// for (Cache cache : Database.Data.Query)
	// {
	// boolean set = true;
	// switch (mSearchState)
	// {
	// case 0:
	// set = cache.Name.toLowerCase().contains(searchPattern);
	// break;
	// case 1:
	// set = cache.GcCode.toLowerCase().contains(searchPattern);
	// break;
	// case 2:
	// set = cache.Owner.toLowerCase().contains(searchPattern) || cache.PlacedBy.toLowerCase().contains(searchPattern);
	// break;
	// }
	//
	// cache.setSearchVisible(set);
	// }
	// }
	// CacheListView.that.getListView().setHasInvisibleItems(true);
	// CacheListView.that.CacheListChangedEvent();
	// }

	// private void clearSearchFilter()
	// {
	// if (!Config.dynamicFilterAtSearch.getValue()) return;
	// synchronized (Database.Data.Query)
	// {
	// for (int i = 0, n = Database.Data.Query.size(); i < n; i++)
	// {
	// Database.Data.Query.get(i).setSearchVisible(true);
	// }
	// }
	// if (CacheListView.that != null)
	// {
	// CacheListView.that.getListView().setHasInvisibleItems(false);
	// CacheListView.that.CacheListChangedEvent();
	// }
	// }

	/**
	 * Sucht mit den Vorgaben nach Caches über die API. Die Gefundenen Caches werden in die DB eingetragen und im Anschluss wird der lokale
	 * Suchvorgang gestartet.
	 */
	private void searchAPI()
	{

		GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler()
		{

			@Override
			public void chekReady(int MemberTypeId)
			{
				int ret = GroundspeakAPI.isValidAPI_Key(true);

				if (ret == 0)
				{
					GL.that.RunOnGL(new IRunOnGL()
					{

						@Override
						public void run()
						{
							GL_MsgBox.Show(Translation.Get("apiKeyNeeded"), Translation.Get("Clue"), MessageBoxButtons.OK,
									MessageBoxIcon.Exclamation, null);
						}
					});

				}
				else if (ret == GroundspeakAPI.CONNECTION_TIMEOUT)
				{
					GL.that.Toast(ConnectionError.INSTANCE);
				}
				else
				{

					wd = CancelWaitDialog.ShowWait(Translation.Get("search"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
					{

						@Override
						public void isCanceld()
						{
							closeWaitDialog();
						}
					}, new Runnable()
					{

						@Override
						public void run()
						{
							int ret = GroundspeakAPI.GetMembershipType();
							if (ret == 3)
							{
								closeWaitDialog();
								searchOnlineNow();
							}
							else if (ret == GroundspeakAPI.CONNECTION_TIMEOUT)
							{
								GL.that.Toast(ConnectionError.INSTANCE);
							}
							else
							{
								GL_MsgBox.Show(Translation.Get("GC_basic"), Translation.Get("GC_title"), MessageBoxButtons.OKCancel,
										MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener()
										{

											@Override
											public boolean onClick(int which, Object data)
											{
												if (which == GL_MsgBox.BUTTON_POSITIVE) searchOnlineNow();
												else
													closeWaitDialog();
												return true;
											}
										});
							}

						}
					});

				}
			}
		});

	}

	CancelWaitDialog wd = null;

	private void closeWaitDialog()
	{
		Logger.DEBUG("SEARCH Close WD");
		if (wd != null) wd.close();
	}

	private void searchOnlineNow()
	{
		Logger.DEBUG("SEARCH Show WD searchOverAPI");
		wd = CancelWaitDialog.ShowWait(Translation.Get("searchOverAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				closeWaitDialog();
			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				Logger.DEBUG("SEARCH Run search overAPI");
				Coordinate searchCoord = null;

				if (MapView.that != null && MapView.that.isVisible())
				{
					searchCoord = MapView.that.center;
				}
				else
				{
					searchCoord = Locator.getCoordinate();
				}

				if (searchCoord == null)
				{
					return;
				}

				// alle per API importierten Caches landen in der Category und
				// GpxFilename
				// API-Import
				// Category suchen, die dazu gehört
				CategoryDAO categoryDAO = new CategoryDAO();
				Category category = categoryDAO.GetCategory(CoreSettingsForward.Categories, "API-Import");
				if (category == null) return; // should not happen!!!

				GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
				if (gpxFilename == null) return;

				ArrayList<Cache> apiCaches = new ArrayList<Cache>();
				ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
				ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

				Search searchC = null;

				String searchPattern = mEingabe.getText().toLowerCase();

				// * 0 = Title <br/>
				// * 1 = Gc-Code <br/>
				// * 2 = Owner <br/>

				switch (mSearchState)
				{
				case 0:
					searchC = new SearchGCName(50, searchCoord, 5000000, searchPattern);
					break;

				case 1:
					searchC = new SearchGC(searchPattern);
					break;

				case 2:
					searchC = new SearchGCOwner(50, searchCoord, 5000000, searchPattern);
					break;
				}

				if (searchC == null)
				{

					return;
				}

				CB_UI.Api.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id);

				if (apiCaches.size() > 0)
				{
					Database.Data.beginTransaction();

					CacheDAO cacheDAO = new CacheDAO();
					LogDAO logDAO = new LogDAO();
					ImageDAO imageDAO = new ImageDAO();
					WaypointDAO waypointDAO = new WaypointDAO();

					int counter = 0;

					synchronized (Database.Data.Query)
					{

						for (Cache cache : apiCaches)
						{
							counter++;
							// cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
							// cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());
							if (Database.Data.Query.GetCacheById(cache.Id) == null)
							{
								Database.Data.Query.add(cache);

								cacheDAO.WriteToDatabase(cache);

								for (LogEntry log : apiLogs)
								{
									if (log.CacheId != cache.Id) continue;
									// Write Log to database
									logDAO.WriteToDatabase(log);
								}

								for (ImageEntry image : apiImages)
								{
									if (image.CacheId != cache.Id) continue;
									// Write Image to database
									imageDAO.WriteToDatabase(image, false);
								}

								for (int i = 0, n = cache.waypoints.size(); i < n; i++)
								{
									Waypoint waypoint = (Waypoint) cache.waypoints.get(i);
									waypointDAO.WriteToDatabase(waypoint);
								}
							}
						}
					}
					Database.Data.setTransactionSuccessful();
					Database.Data.endTransaction();

					Database.Data.GPXFilenameUpdateCacheCount();

					CachListChangedEventList.Call();

					if (counter == 1)
					{
						// select this Cache
						Cache cache = Database.Data.Query.GetCacheById(apiCaches.get(0).Id);
						GlobalCore.setSelectedCache(cache);
					}

				}
				Logger.DEBUG("SEARCH Run search overAPI ready");
				closeWaitDialog();
			}
		});
	}

	/**
	 * setzt den Filter auf die Such Anfrage
	 */
	private void setFilter()
	{
		String searchPattern = mEingabe.getText().toLowerCase();

		GlobalCore.LastFilter.filterName = "";
		GlobalCore.LastFilter.filterGcCode = "";
		GlobalCore.LastFilter.filterOwner = "";

		if (mSearchState == 0) GlobalCore.LastFilter.filterName = searchPattern;
		else if (mSearchState == 1) GlobalCore.LastFilter.filterGcCode = searchPattern;
		if (mSearchState == 2) GlobalCore.LastFilter.filterOwner = searchPattern;

		ApplyFilter();
	}

	public void ApplyFilter()
	{
		ApplyFilter(GlobalCore.LastFilter);
	}

	public void ApplyFilter(FilterProperties filter)
	{
		EditFilterSettings.ApplyFilter(filter);
	}

	public enum searchMode
	{
		Titel, GcCode, Owner
	}

	public void addSearch(String searchPattern, searchMode Mode)
	{

		Logger.DEBUG("addSearch " + searchPattern);

		mEingabe.setText(searchPattern);
		switchSearcheMode(Mode.ordinal());

		// auf Online schalten
		mTglBtnOnline.setState(1);
		setFilterBtnState();

		// Suche auslösen
		mBtnSearch.performClick();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void onShow()
	{
		try
		{

			if (TabMainView.cacheListView != null)
			{
				setY(TabMainView.cacheListView.getMaxY() - this.getHeight());
				TabMainView.cacheListView.setTopPlaceHolder(this.getHeight());
			}

			if (!GL.that.PopUpIsShown()) that.showNotCloseAutomaticly();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Slider.that.registerPosChangedEvent(listner);
	}

	@Override
	public void onHide()
	{
		Slider.that.removePosChangedEvent(listner);

		if (TabMainView.cacheListView != null)
		{
			TabMainView.cacheListView.resetPlaceHolder();
		}
	}

	private YPositionChanged listner = new YPositionChanged()
	{

		@Override
		public void Position(float SliderTop, float SliderBottom)
		{

			if (TabMainView.cacheListView != null)
			{
				setY(TabMainView.cacheListView.getMaxY() - that.getHeight());
			}
		}
	};

	GL_MsgBox MSB;

	private void askPremium()
	{

		// First check API-Key with visual Feedback
		GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler()
		{

			@Override
			public void chekReady(int MemberType)
			{
				int ret = GroundspeakAPI.isValidAPI_Key(true);
				Logger.DEBUG("SEARCH isValidAPI_Key ret=" + ret);
				if (ret == 0)
				{
					GL.that.RunOnGL(new IRunOnGL()
					{

						@Override
						public void run()
						{

							GL_MsgBox.Show(Translation.Get("apiKeyNeeded"), Translation.Get("Clue"), MessageBoxButtons.OK,
									MessageBoxIcon.Exclamation, null);
						}
					});

				}
				else if (ret == GroundspeakAPI.CONNECTION_TIMEOUT)
				{
					GL.that.RunOnGL(new IRunOnGL()
					{
						@Override
						public void run()
						{
							GL_MsgBox.Show(Translation.Get("noInetMsg"), Translation.Get("noInetTitle"), MessageBoxButtons.OK,
									MessageBoxIcon.Error, null);
						}
					});
				}
				else
				{
					if (ret == 3)
					{
						// searchOnlineNow();
						showTargetApiDialog();
					}
					else
					{
						closeWD();

						GL.that.RunOnGL(new IRunOnGL()
						{

							@Override
							public void run()
							{
								MSB = GL_MsgBox.Show(Translation.Get("GC_basic"), Translation.Get("GC_title"), MessageBoxButtons.OKCancel,
										MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener()
										{

											@Override
											public boolean onClick(int which, Object data)
											{
												closeMsgBox();
												if (which == GL_MsgBox.BUTTON_POSITIVE)
												{
													showTargetApiDialog();
												}

												return true;
											}
										});
							}
						});

					}
				}
			}
		});

	}

	private void closeMsgBox()
	{
		MSB.close();
	}

	private void closeWD()
	{
		if (wd != null) wd.close();
	}

	private void showTargetApiDialog()
	{
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				SearchOverPosition.ShowInstanz();
			}
		});

	}

	@Override
	public void dispose()
	{
		// do nothing is static dialog
	}

}
